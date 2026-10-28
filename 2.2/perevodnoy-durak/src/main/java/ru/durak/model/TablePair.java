package ru.durak.model;

import java.util.Objects;

/**
 * Пара на столе: атакующая карта и (опционально) карта отбоя.
 */
public final class TablePair {

    private final Card attack;
    private Card defense; // null, если ещё не отбита

    public TablePair(Card attack) {
        this.attack = Objects.requireNonNull(attack);
        this.defense = null;
    }

    public Card getAttack() {
        return attack;
    }

    public Card getDefense() {
        return defense;
    }

    public boolean isBeaten() {
        return defense != null;
    }

    public void setDefense(Card defense) {
        if (this.defense != null) {
            throw new IllegalStateException("Карта уже отбита");
        }
        this.defense = Objects.requireNonNull(defense);
    }

    @Override
    public String toString() {
        if (defense == null) {
            return attack.toString();
        }
        return attack + " ← " + defense;
    }
}
