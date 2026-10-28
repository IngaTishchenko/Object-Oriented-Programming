package ru.durak.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Компаратор карт с учётом козырной масти.
 * Козырь бьёт любую некозырную карту.
 * Среди козырей и среди карт одной масти — по достоинству.
 */
public final class CardComparator implements Comparator<Card> {

    private final Suit trump;

    public CardComparator(Suit trump) {
        this.trump = Objects.requireNonNull(trump, "trump");
    }

    public Suit getTrump() {
        return trump;
    }

    /**
     * Возвращает true, если attacker бьёт target.
     * Карта бьёт другую, если:
     * - та же масть и старше, или
     * - attacker — козырь, а target — нет, или
     * - обе козыри и attacker старше.
     */
    public boolean beats(Card attacker, Card target) {
        if (attacker.isTrump(trump) && !target.isTrump(trump)) {
            return true;
        }
        if (!attacker.isTrump(trump) && target.isTrump(trump)) {
            return false;
        }
        // одна масть (обе козыри или обе нет)
        if (attacker.getSuit() == target.getSuit()) {
            return attacker.getRank().getValue() > target.getRank().getValue();
        }
        return false;
    }

    @Override
    public int compare(Card a, Card b) {
        boolean aTrump = a.isTrump(trump);
        boolean bTrump = b.isTrump(trump);
        if (aTrump && !bTrump) return 1;
        if (!aTrump && bTrump) return -1;
        return Integer.compare(a.getRank().getValue(), b.getRank().getValue());
    }
}
