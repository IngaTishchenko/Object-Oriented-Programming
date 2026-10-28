package ru.durak.game;

import ru.durak.model.*;

import java.util.*;

/**
 * Движок правил Переводного дурака.
 * Не печатает в консоль — только изменяет состояние и возвращает события.
 *
 * Правила:
 * - Колода 36 карт, раздача по 6.
 * - Подкидной + перевод хода картой того же достоинства.
 * - Ограничение перевода: число карт на столе + 1 ≤ число карт у следующего игрока.
 * - В первом раунде перевод запрещён.
 * - Перевод возможен только до начала отбоя (пока ни одна карта не отбита).
 * - Подкидывание — карты достоинств, уже лежащих на столе, не больше карт у защищающегося.
 */
public class GameEngine {

    private final GameState state;
    private final List<GameEvent> lastEvents = new ArrayList<>();

    public GameEngine(GameState state) {
        this.state = Objects.requireNonNull(state);
    }

    public GameState getState() {
        return state;
    }

    public List<GameEvent> getLastEvents() {
        return Collections.unmodifiableList(lastEvents);
    }

    /**
     * Создаёт новую партию.
     *
     * @param playerNames имена игроков (2–6)
     * @param random      генератор с заданным зерном
     */
    public static GameEngine createNewGame(List<String> playerNames, Random random) {
        if (playerNames == null || playerNames.size() < 2 || playerNames.size() > 6) {
            throw new IllegalArgumentException("Нужно от 2 до 6 игроков");
        }

        List<Player> players = playerNames.stream().map(Player::new).toList();
        Deck deck = new Deck(random);
        deck.shuffle();

        // раздача
        for (int i = 0; i < 6; i++) {
            for (Player p : players) {
                p.addCard(deck.draw());
            }
        }

        Card trumpCard = deck.peek(); // остаётся в колоде, масть — козырь
        Suit trump = trumpCard.getSuit();

        // первый ходит владелец младшего козыря
        int firstAttacker = findLowestTrumpHolder(players, trump);

        GameState state = new GameState(players, deck, trump, trumpCard, firstAttacker);
        GameEngine engine = new GameEngine(state);

        engine.lastEvents.add(new GameEvent.GameStarted(playerNames, trump, trumpCard));
        for (Player p : players) {
            engine.lastEvents.add(new GameEvent.CardsDealt(p.getName(), 6));
        }
        return engine;
    }

    private static int findLowestTrumpHolder(List<Player> players, Suit trump) {
        Card lowest = null;
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            for (Card c : players.get(i).getHand()) {
                if (c.isTrump(trump)) {
                    if (lowest == null || c.getRank().getValue() < lowest.getRank().getValue()) {
                        lowest = c;
                        index = i;
                    }
                }
            }
        }
        return index;
    }

    /**
     * Применяет ход. При ошибке бросает {@link IllegalMoveException}.
     *
     * @return список событий, произошедших в результате хода
     */
    public List<GameEvent> apply(Move move) {
        lastEvents.clear();
        validateAndApply(move);
        checkFinishedPlayers();
        checkGameOver();
        return List.copyOf(lastEvents);
    }

    private void validateAndApply(Move move) {
        Player player = move.getPlayer();
        if (player.isFinished()) {
            throw new IllegalMoveException("Игрок уже вышел из игры");
        }

        switch (move.getType()) {
            case ATTACK -> applyAttack(move);
            case DEFEND -> applyDefend(move);
            case TRANSFER -> applyTransfer(move);
            case THROW_IN -> applyThrowIn(move);
            case TAKE -> applyTake(move);
            case PASS -> applyPass(move);
            default -> throw new IllegalMoveException("Неизвестный тип хода");
        }
    }

    private void applyAttack(Move move) {
        if (state.getPhase() != GamePhase.WAITING_ATTACK) {
            throw new IllegalMoveException("Сейчас нельзя атаковать");
        }
        if (move.getPlayer() != state.getAttacker()) {
            throw new IllegalMoveException("Ходит не атакующий");
        }

        List<Card> cards = move.getCards();
        Rank rank = cards.get(0).getRank();
        for (Card c : cards) {
            if (c.getRank() != rank) {
                throw new IllegalMoveException("Все карты атаки должны быть одного достоинства");
            }
            if (!move.getPlayer().hasCard(c)) {
                throw new IllegalMoveException("У игрока нет карты " + c);
            }
        }

        // ограничение: не больше карт, чем у защищающегося
        if (cards.size() > state.getDefender().handSize()) {
            throw new IllegalMoveException("Нельзя ходить большим числом карт, чем у защищающегося");
        }

        for (Card c : cards) {
            move.getPlayer().removeCard(c);
            state.addToTable(c);
        }

        state.setPhase(GamePhase.DEFENDING);
        lastEvents.add(new GameEvent.AttackStarted(move.getPlayer().getName(), cards));
    }

    private void applyDefend(Move move) {
        if (state.getPhase() != GamePhase.DEFENDING && state.getPhase() != GamePhase.THROWING) {
            throw new IllegalMoveException("Сейчас нельзя отбиваться");
        }
        if (move.getPlayer() != state.getDefender()) {
            throw new IllegalMoveException("Ходит не защищающийся");
        }

        Card defense = move.getCards().get(0);
        Card target = move.getTarget();

        if (!move.getPlayer().hasCard(defense)) {
            throw new IllegalMoveException("У игрока нет карты " + defense);
        }

        TablePair pair = findUnbeaten(target);
        if (pair == null) {
            throw new IllegalMoveException("Карта " + target + " не лежит на столе или уже отбита");
        }

        if (!state.getComparator().beats(defense, target)) {
            throw new IllegalMoveException(defense + " не бьёт " + target);
        }

        move.getPlayer().removeCard(defense);
        pair.setDefense(defense);
        lastEvents.add(new GameEvent.CardDefended(move.getPlayer().getName(), target, defense));

        // если все отбиты — переходим к подкидыванию
        if (state.unbeatenCount() == 0) {
            state.setPhase(GamePhase.THROWING);
        }
    }

    private TablePair findUnbeaten(Card attackCard) {
        for (TablePair p : state.getTable()) {
            if (!p.isBeaten() && p.getAttack().equals(attackCard)) {
                return p;
            }
        }
        return null;
    }

    private void applyTransfer(Move move) {
        if (state.getPhase() != GamePhase.DEFENDING) {
            throw new IllegalMoveException("Перевод возможен только в фазе защиты");
        }
        if (move.getPlayer() != state.getDefender()) {
            throw new IllegalMoveException("Переводить может только защищающийся");
        }
        if (state.isFirstRound()) {
            throw new IllegalMoveException("В первом раунде перевод запрещён");
        }
        // перевод только до начала отбоя
        if (state.getTable().stream().anyMatch(TablePair::isBeaten)) {
            throw new IllegalMoveException("После начала отбоя перевод невозможен");
        }

        Card transferCard = move.getCards().get(0);
        if (!move.getPlayer().hasCard(transferCard)) {
            throw new IllegalMoveException("У игрока нет карты " + transferCard);
        }

        // карта должна совпадать по достоинству с картами на столе (все одного ранга пока)
        Rank required = state.getTable().get(0).getAttack().getRank();
        if (transferCard.getRank() != required) {
            throw new IllegalMoveException("Перевод возможен только картой того же достоинства: " + required);
        }

        // ограничение по числу карт у следующего игрока
        int nextIdx = state.nextActiveIndex(state.getDefenderIndex());
        Player nextPlayer = state.getPlayers().get(nextIdx);
        int cardsOnTable = state.getTable().size(); // после перевода станет +1
        if (cardsOnTable + 1 > nextPlayer.handSize()) {
            throw new IllegalMoveException(
                    "Нельзя перевести: у следующего игрока (" + nextPlayer.getName() +
                    ") только " + nextPlayer.handSize() + " карт, а на столе станет " + (cardsOnTable + 1));
        }

        move.getPlayer().removeCard(transferCard);
        state.addToTable(transferCard);

        // меняем защищающегося
        int oldDefender = state.getDefenderIndex();
        state.setDefenderIndex(nextIdx);
        // атакующий остаётся прежним (или можно считать, что все предыдущие — атакующие)

        lastEvents.add(new GameEvent.Transferred(
                move.getPlayer().getName(),
                nextPlayer.getName(),
                transferCard,
                state.allTableCards()));
    }

    private void applyThrowIn(Move move) {
        if (state.getPhase() != GamePhase.THROWING && state.getPhase() != GamePhase.DEFENDING) {
            throw new IllegalMoveException("Сейчас нельзя подкидывать");
        }

        Player player = move.getPlayer();
        // подкидывать может любой активный, кроме защищающегося (обычно атакующий и остальные)
        if (player == state.getDefender()) {
            throw new IllegalMoveException("Защищающийся не подкидывает");
        }
        if (player.isFinished()) {
            throw new IllegalMoveException("Игрок уже вышел");
        }

        List<Card> cards = move.getCards();
        List<Rank> allowed = state.ranksOnTable();
        for (Card c : cards) {
            if (!allowed.contains(c.getRank())) {
                throw new IllegalMoveException("Можно подкидывать только достоинства, уже лежащие на столе: " + allowed);
            }
            if (!player.hasCard(c)) {
                throw new IllegalMoveException("У игрока нет карты " + c);
            }
        }

        int freeSlots = state.getDefender().handSize() - state.unbeatenCount();
        if (cards.size() > freeSlots) {
            throw new IllegalMoveException("Нельзя подкинуть больше карт, чем осталось у защищающегося");
        }

        for (Card c : cards) {
            player.removeCard(c);
            state.addToTable(c);
        }

        // после подкидывания снова фаза защиты, если есть неотбитые
        if (state.unbeatenCount() > 0) {
            state.setPhase(GamePhase.DEFENDING);
        }

        lastEvents.add(new GameEvent.ThrownIn(player.getName(), cards));
    }

    private void applyTake(Move move) {
        if (state.getPhase() != GamePhase.DEFENDING && state.getPhase() != GamePhase.THROWING) {
            throw new IllegalMoveException("Сейчас нельзя брать");
        }
        if (move.getPlayer() != state.getDefender()) {
            throw new IllegalMoveException("Брать может только защищающийся");
        }

        List<Card> taken = state.allTableCards();
        state.getDefender().addCards(taken);
        lastEvents.add(new GameEvent.Taken(move.getPlayer().getName(), taken));

        state.clearTable();
        refillHands();

        // следующий ходит игрок слева от того, кто взял
        int newAttacker = state.nextActiveIndex(state.getDefenderIndex());
        state.setAttackerIndex(newAttacker);
        state.setDefenderIndex(state.nextActiveIndex(newAttacker));
        state.setPhase(GamePhase.WAITING_ATTACK);
        state.setFirstRound(false);
    }

    private void applyPass(Move move) {
        // PASS от атакующих означает «больше не подкидываю»
        if (state.getPhase() != GamePhase.THROWING) {
            throw new IllegalMoveException("Пас возможен только в фазе подкидывания");
        }

        // если все отбиты и никто больше не хочет подкидывать — кон окончен успешно
        if (state.unbeatenCount() == 0) {
            finishSuccessfulDefense();
        } else {
            // ещё есть неотбитые — защищающийся должен либо добить, либо взять
            // пас от атакующего ничего не меняет, пока не все спасовали
            // для простоты: если ходит не защищающийся — просто игнорируем как «не подкидываю»
        }
    }

    /**
     * Завершает успешную защиту (все карты отбиты, подкидывать больше не хотят).
     * Вызывается извне, когда атакующие пасуют.
     */
    public List<GameEvent> confirmSuccessfulDefense() {
        lastEvents.clear();
        if (state.getPhase() != GamePhase.THROWING || state.unbeatenCount() > 0) {
            throw new IllegalMoveException("Нельзя завершить защиту: есть неотбитые карты или неверная фаза");
        }
        finishSuccessfulDefense();
        checkFinishedPlayers();
        checkGameOver();
        return List.copyOf(lastEvents);
    }

    private void finishSuccessfulDefense() {
        // карты уходят в отбой (просто очищаем стол)
        state.clearTable();
        lastEvents.add(new GameEvent.RoundWon(state.getDefender().getName()));

        refillHands();

        // следующий атакующий — бывший защищающийся
        int newAttacker = state.getDefenderIndex();
        state.setAttackerIndex(newAttacker);
        state.setDefenderIndex(state.nextActiveIndex(newAttacker));
        state.setPhase(GamePhase.WAITING_ATTACK);
        state.setFirstRound(false);
    }

    private void refillHands() {
        // добирают до 6, начиная с атакующего, затем по кругу, защищающийся — последним
        List<Integer> order = new ArrayList<>();
        int idx = state.getAttackerIndex();
        for (int i = 0; i < state.getPlayers().size(); i++) {
            order.add(idx);
            idx = state.nextActiveIndex(idx);
        }
        // защищающийся должен быть последним
        int defIdx = state.getDefenderIndex();
        order.removeIf(i -> i == defIdx);
        order.add(defIdx);

        for (int i : order) {
            Player p = state.getPlayers().get(i);
            if (p.isFinished()) continue;
            while (p.handSize() < 6 && !state.getDeck().isEmpty()) {
                p.addCard(state.getDeck().draw());
                lastEvents.add(new GameEvent.CardsDrawn(p.getName(), 1));
            }
        }
    }

    private void checkFinishedPlayers() {
        for (Player p : state.getPlayers()) {
            if (!p.isFinished() && p.handSize() == 0 && state.getDeck().isEmpty()) {
                p.setFinished(true);
                lastEvents.add(new GameEvent.PlayerFinished(p.getName()));
            }
        }
    }

    private void checkGameOver() {
        List<Player> active = state.activePlayers();
        if (active.size() <= 1) {
            state.setPhase(GamePhase.FINISHED);
            if (active.size() == 1) {
                state.setFool(active.get(0).getName());
                lastEvents.add(new GameEvent.GameOver(active.get(0).getName()));
            } else {
                // все вышли одновременно — ничья, дурака нет
                state.setFool(null);
                lastEvents.add(new GameEvent.GameOver(null));
            }
        }
    }

    /**
     * Возвращает список допустимых ходов для текущего игрока (для ботов и UI).
     */
    public List<Move> getLegalMoves(Player player) {
        List<Move> moves = new ArrayList<>();
        if (state.isGameOver() || player.isFinished()) {
            return moves;
        }

        GamePhase phase = state.getPhase();

        if (phase == GamePhase.WAITING_ATTACK && player == state.getAttacker()) {
            // все возможные атаки одним достоинством
            Map<Rank, List<Card>> byRank = new EnumMap<>(Rank.class);
            for (Card c : player.getHand()) {
                byRank.computeIfAbsent(c.getRank(), r -> new ArrayList<>()).add(c);
            }
            int max = state.getDefender().handSize();
            for (List<Card> group : byRank.values()) {
                for (int k = 1; k <= Math.min(group.size(), max); k++) {
                    moves.add(Move.attack(player, group.subList(0, k)));
                }
            }
        }

        if ((phase == GamePhase.DEFENDING || phase == GamePhase.THROWING) && player == state.getDefender()) {
            // отбой каждой неотбитой карты
            for (TablePair pair : state.getTable()) {
                if (!pair.isBeaten()) {
                    for (Card c : player.getHand()) {
                        if (state.getComparator().beats(c, pair.getAttack())) {
                            moves.add(Move.defend(player, c, pair.getAttack()));
                        }
                    }
                }
            }
            // перевод
            if (phase == GamePhase.DEFENDING
                    && !state.isFirstRound()
                    && state.getTable().stream().noneMatch(TablePair::isBeaten)) {
                Rank required = state.getTable().get(0).getAttack().getRank();
                int nextIdx = state.nextActiveIndex(state.getDefenderIndex());
                Player next = state.getPlayers().get(nextIdx);
                if (state.getTable().size() + 1 <= next.handSize()) {
                    for (Card c : player.cardsOfRank(required)) {
                        moves.add(Move.transfer(player, c));
                    }
                }
            }
            // взять
            moves.add(Move.take(player));
        }

        if ((phase == GamePhase.DEFENDING || phase == GamePhase.THROWING) && player != state.getDefender()) {
            // подкидывание
            List<Rank> allowed = state.ranksOnTable();
            int free = state.getDefender().handSize() - state.unbeatenCount();
            if (free > 0) {
                List<Card> candidates = player.getHand().stream()
                        .filter(c -> allowed.contains(c.getRank()))
                        .toList();
                // все непустые подмножества до free (упрощённо — по одной и все)
                for (Card c : candidates) {
                    moves.add(Move.throwIn(player, List.of(c)));
                }
                if (candidates.size() > 1 && candidates.size() <= free) {
                    moves.add(Move.throwIn(player, candidates));
                }
            }
            if (phase == GamePhase.THROWING && state.unbeatenCount() == 0) {
                moves.add(Move.pass(player));
            }
        }

        return moves;
    }
}
