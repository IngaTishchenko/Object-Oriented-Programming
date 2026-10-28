package ru.durak;

import org.junit.jupiter.api.Test;
import ru.durak.model.*;

import static org.junit.jupiter.api.Assertions.*;

class CardComparatorTest {

    private final Suit trump = Suit.HEARTS;
    private final CardComparator cmp = new CardComparator(trump);

    @Test
    void trumpBeatsNonTrump() {
        Card trump6 = new Card(Rank.SIX, Suit.HEARTS);
        Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
        assertTrue(cmp.beats(trump6, aceSpades));
        assertFalse(cmp.beats(aceSpades, trump6));
    }

    @Test
    void higherSameSuitBeats() {
        Card nine = new Card(Rank.NINE, Suit.CLUBS);
        Card seven = new Card(Rank.SEVEN, Suit.CLUBS);
        assertTrue(cmp.beats(nine, seven));
        assertFalse(cmp.beats(seven, nine));
    }

    @Test
    void differentNonTrumpDoesNotBeat() {
        Card nineClubs = new Card(Rank.NINE, Suit.CLUBS);
        Card sevenSpades = new Card(Rank.SEVEN, Suit.SPADES);
        assertFalse(cmp.beats(nineClubs, sevenSpades));
        assertFalse(cmp.beats(sevenSpades, nineClubs));
    }

    @Test
    void higherTrumpBeatsLowerTrump() {
        Card aceTrump = new Card(Rank.ACE, Suit.HEARTS);
        Card sixTrump = new Card(Rank.SIX, Suit.HEARTS);
        assertTrue(cmp.beats(aceTrump, sixTrump));
        assertFalse(cmp.beats(sixTrump, aceTrump));
    }

    @Test
    void comparatorOrdering() {
        Card sixTrump = new Card(Rank.SIX, Suit.HEARTS);
        Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
        assertTrue(cmp.compare(sixTrump, aceSpades) > 0);
    }
}
