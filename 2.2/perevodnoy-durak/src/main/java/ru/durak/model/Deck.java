package ru.durak.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Колода из 36 карт.
 * Использует заданный генератор случайных чисел для воспроизводимости.
 */
public class Deck {

    private final List<Card> cards;
    private final Random random;

    public Deck(Random random) {
        this.random = random;
        this.cards = new ArrayList<>(36);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    /**
     * Перемешивает колоду.
     */
    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    /**
     * Берёт верхнюю карту (с конца списка).
     */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Колода пуста");
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Смотрит верхнюю карту без извлечения (козырь).
     */
    public Card peek() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Колода пуста");
        }
        return cards.get(cards.size() - 1);
    }

    /**
     * Возвращает неизменяемую копию оставшихся карт (для отладки/тестов).
     */
    public List<Card> remaining() {
        return List.copyOf(cards);
    }
}
