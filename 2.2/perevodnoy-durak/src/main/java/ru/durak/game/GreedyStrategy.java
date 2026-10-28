package ru.durak.game;

import ru.durak.model.*;

import java.util.Comparator;
import java.util.List;

/**
 * Жадный бот: бьёт минимальной подходящей картой, переводит если выгодно,
 * подкидывает мелкие, берёт только если нет другого выхода.
 */
public class GreedyStrategy implements PlayerStrategy {

    private final CardComparator comparator;

    public GreedyStrategy(Suit trump) {
        this.comparator = new CardComparator(trump);
    }

    @Override
    public Move chooseMove(Player player, GameState state, List<Move> legal) {
        if (legal.isEmpty()) {
            return null;
        }

        // 1. Попробуем перевести
        for (Move m : legal) {
            if (m.getType() == MoveType.TRANSFER) {
                return m;
            }
        }

        // 2. Отбиваемся минимальной картой
        Move bestDefend = legal.stream()
                .filter(m -> m.getType() == MoveType.DEFEND)
                .min(Comparator.comparingInt(m -> m.getCards().get(0).getRank().getValue()))
                .orElse(null);
        if (bestDefend != null) {
            return bestDefend;
        }

        // 3. Атакуем / подкидываем минимальными
        Move bestAttack = legal.stream()
                .filter(m -> m.getType() == MoveType.ATTACK || m.getType() == MoveType.THROW_IN)
                .min(Comparator.comparingInt(m -> m.getCards().get(0).getRank().getValue()))
                .orElse(null);
        if (bestAttack != null) {
            return bestAttack;
        }

        // 4. Пас
        for (Move m : legal) {
            if (m.getType() == MoveType.PASS) {
                return m;
            }
        }

        // 5. Берём
        return legal.get(0);
    }
}
