package com.elevator.model;

/**
 * Направление движения лифта или заявки.
 */

public enum Direction {
    /** Вверх. */
    UP,
    /** Вниз. */
    DOWN;

        /**
         * Возвращает противоположное направление.
         *
         * @return {@link #DOWN}, если текущее {@link #UP}, и наоборот
         */

    public Direction opposite() {
        return this == UP ? DOWN : UP;
    }
}