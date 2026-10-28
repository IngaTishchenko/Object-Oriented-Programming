package ru.durak.game;

import ru.durak.model.GameState;
import ru.durak.model.Move;
import ru.durak.model.Player;

import java.util.List;

/**
 * Паттерн Strategy: стратегия выбора хода игроком (бот или человек).
 */
public interface PlayerStrategy {

    /**
     * Выбирает ход из списка допустимых.
     *
     * @param player  текущий игрок
     * @param state   состояние игры
     * @param legal   допустимые ходы
     * @return выбранный ход или null, если ходить не нужно
     */
    Move chooseMove(Player player, GameState state, List<Move> legal);
}
