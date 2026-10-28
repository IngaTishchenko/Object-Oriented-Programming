package ru.durak.model;

import java.util.Objects;

/**
 * Игральная карта.
 * Неизменяемый объект.
 */
public final class Card implements Comparable<Card> {

    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = Objects.requireNonNull(rank, "rank");
        this.suit = Objects.requireNonNull(suit, "suit");
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    /**
     * Является ли карта козырем.
     */
    public boolean isTrump(Suit trump) {
        return suit == trump;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    @Override
    public String toString() {
        return rank.getDisplay() + suit.getSymbol();
    }

    /**
     * Сравнение без учёта козыря (по достоинству, затем по масти).
     * Для сравнения с учётом козыря используйте {@link CardComparator}.
     */
    @Override
    public int compareTo(Card other) {
        int byRank = Integer.compare(this.rank.getValue(), other.rank.getValue());
        if (byRank != 0) return byRank;
        return this.suit.compareTo(other.suit);
    }
}
