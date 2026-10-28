package ru.durak.model;

import java.util.List;
import java.util.Objects;

/**
 * Ход игрока.
 * Неизменяемый.
 */
public final class Move {

    private final MoveType type;
    private final Player player;
    private final List<Card> cards; // для ATTACK, DEFEND, TRANSFER, THROW_IN
    private final Card target;      // для DEFEND — какую карту бьём

    private Move(MoveType type, Player player, List<Card> cards, Card target) {
        this.type = Objects.requireNonNull(type);
        this.player = Objects.requireNonNull(player);
        this.cards = cards == null ? List.of() : List.copyOf(cards);
        this.target = target;
    }

    public static Move attack(Player player, List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Атака должна содержать хотя бы одну карту");
        }
        return new Move(MoveType.ATTACK, player, cards, null);
    }

    public static Move defend(Player player, Card defenseCard, Card attackCard) {
        return new Move(MoveType.DEFEND, player, List.of(defenseCard), attackCard);
    }

    public static Move transfer(Player player, Card transferCard) {
        return new Move(MoveType.TRANSFER, player, List.of(transferCard), null);
    }

    public static Move throwIn(Player player, List<Card> cards) {
        return new Move(MoveType.THROW_IN, player, cards, null);
    }

    public static Move take(Player player) {
        return new Move(MoveType.TAKE, player, null, null);
    }

    public static Move pass(Player player) {
        return new Move(MoveType.PASS, player, null, null);
    }

    public MoveType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Card> getCards() {
        return cards;
    }

    public Card getTarget() {
        return target;
    }

    @Override
    public String toString() {
        String who = player.getName();
        boolean vy = "Вы".equals(who);
        return switch (type) {
            case ATTACK -> who + (vy ? " атакуете " : " атакует ") + cards;
            case DEFEND -> who + (vy ? " бьёте " : " бьёт ") + target + " картой " + cards.get(0);
            case TRANSFER -> who + (vy ? " переводите " : " переводит ") + cards.get(0);
            case THROW_IN -> who + (vy ? " подкидываете " : " подкидывает ") + cards;
            case TAKE -> who + (vy ? " берёте" : " берёт");
            case PASS -> who + (vy ? " пасуете" : " пас");
        };
    }
}
