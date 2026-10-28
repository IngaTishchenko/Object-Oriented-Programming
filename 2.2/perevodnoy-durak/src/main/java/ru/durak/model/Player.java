package ru.durak.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Игрок с рукой карт.
 */
public class Player {

    private final String name;
    private final List<Card> hand;
    private boolean finished;

    public Player(String name) {
        this.name = Objects.requireNonNull(name);
        this.hand = new ArrayList<>();
        this.finished = false;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public int handSize() {
        return hand.size();
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void addCards(List<Card> cards) {
        hand.addAll(cards);
    }

    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    public boolean hasCard(Card card) {
        return hand.contains(card);
    }

    /**
     * Есть ли в руке карта данного достоинства.
     */
    public boolean hasRank(Rank rank) {
        return hand.stream().anyMatch(c -> c.getRank() == rank);
    }

    /**
     * Карты данного достоинства.
     */
    public List<Card> cardsOfRank(Rank rank) {
        return hand.stream().filter(c -> c.getRank() == rank).toList();
    }

    public void clearHand() {
        hand.clear();
    }

    @Override
    public String toString() {
        return name + " (" + hand.size() + ")";
    }
}
