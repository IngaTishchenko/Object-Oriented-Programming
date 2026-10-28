package ru.durak.model;

/**
 * Масть карты.
 */
public enum Suit {
    HEARTS("♥", "черви"),
    DIAMONDS("♦", "бубны"),
    CLUBS("♣", "трефы"),
    SPADES("♠", "пики");

    private final String symbol;
    private final String russianName;

    Suit(String symbol, String russianName) {
        this.symbol = symbol;
        this.russianName = russianName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getRussianName() {
        return russianName;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
