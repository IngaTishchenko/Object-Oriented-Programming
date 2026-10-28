package ru.durak.game;

import ru.durak.model.GameState;
import ru.durak.model.Move;
import ru.durak.model.MoveType;
import ru.durak.model.Player;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Случайный бот: выбирает случайный допустимый ход.
 * Предпочитает не брать, если есть способ отбиться или перевести.
 */
public class RandomStrategy implements PlayerStrategy {

    private final Random random;

    public RandomStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Move chooseMove(Player player, GameState state, List<Move> legal) {
        if (legal.isEmpty()) {
            return null;
        }

        // предпочитаем не TAKE, если есть альтернативы
        List<Move> preferred = legal.stream()
                .filter(m -> m.getType() != MoveType.TAKE)
                .collect(Collectors.toList());

        List<Move> pool = preferred.isEmpty() ? legal : preferred;
        return pool.get(random.nextInt(pool.size()));
    }
}
