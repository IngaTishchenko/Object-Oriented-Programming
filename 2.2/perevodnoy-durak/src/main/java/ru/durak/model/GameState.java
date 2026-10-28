package ru.durak.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Состояние партии.
 * Механика работает с этим объектом; вывод — отдельный слой.
 */
public class GameState {

    private final List<Player> players;
    private final Deck deck;
    private final Suit trump;
    private final Card trumpCard; // открытый козырь
    private final CardComparator comparator;

    private final List<TablePair> table = new ArrayList<>();
    private GamePhase phase = GamePhase.WAITING_ATTACK;

    private int attackerIndex;
    private int defenderIndex;
    private boolean firstRound = true; // в первом раунде перевод запрещён

    private String fool; // имя проигравшего

    public GameState(List<Player> players, Deck deck, Suit trump, Card trumpCard, int firstAttackerIndex) {
        this.players = List.copyOf(players);
        this.deck = deck;
        this.trump = trump;
        this.trumpCard = trumpCard;
        this.comparator = new CardComparator(trump);
        this.attackerIndex = firstAttackerIndex;
        this.defenderIndex = nextActiveIndex(firstAttackerIndex);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Deck getDeck() {
        return deck;
    }

    public Suit getTrump() {
        return trump;
    }

    public Card getTrumpCard() {
        return trumpCard;
    }

    public CardComparator getComparator() {
        return comparator;
    }

    public List<TablePair> getTable() {
        return Collections.unmodifiableList(table);
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public Player getAttacker() {
        return players.get(attackerIndex);
    }

    public Player getDefender() {
        return players.get(defenderIndex);
    }

    public int getAttackerIndex() {
        return attackerIndex;
    }

    public int getDefenderIndex() {
        return defenderIndex;
    }

    public boolean isFirstRound() {
        return firstRound;
    }

    public void setFirstRound(boolean firstRound) {
        this.firstRound = firstRound;
    }

    public String getFool() {
        return fool;
    }

    public void setFool(String fool) {
        this.fool = fool;
    }

    public void addToTable(Card attackCard) {
        table.add(new TablePair(attackCard));
    }

    public void clearTable() {
        table.clear();
    }

    public int unbeatenCount() {
        return (int) table.stream().filter(p -> !p.isBeaten()).count();
    }

    public List<Card> allTableCards() {
        List<Card> result = new ArrayList<>();
        for (TablePair p : table) {
            result.add(p.getAttack());
            if (p.getDefense() != null) {
                result.add(p.getDefense());
            }
        }
        return result;
    }

    public List<Rank> ranksOnTable() {
        return allTableCards().stream()
                .map(Card::getRank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Следующий активный игрок по кругу.
     */
    public int nextActiveIndex(int from) {
        int n = players.size();
        for (int i = 1; i <= n; i++) {
            int idx = (from + i) % n;
            if (!players.get(idx).isFinished()) {
                return idx;
            }
        }
        return from; // все закончили
    }

    public void setAttackerIndex(int idx) {
        this.attackerIndex = idx;
    }

    public void setDefenderIndex(int idx) {
        this.defenderIndex = idx;
    }

    public List<Player> activePlayers() {
        return players.stream().filter(p -> !p.isFinished()).toList();
    }

    public int activeCount() {
        return (int) players.stream().filter(p -> !p.isFinished()).count();
    }

    public boolean isGameOver() {
        return phase == GamePhase.FINISHED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Фаза: ").append(phase)
          .append(", козырь: ").append(trump).append(" (").append(trumpCard).append(")")
          .append(", колода: ").append(deck.size())
          .append("\nАтакует: ").append(getAttacker().getName())
          .append(", защищается: ").append(getDefender().getName())
          .append("\nСтол: ").append(table)
          .append("\nИгроки:\n");
        for (Player p : players) {
            sb.append("  ").append(p.getName()).append(": ").append(p.getHand()).append("\n");
        }
        return sb.toString();
    }
}
