package com.elevator.model;

/**
 * Состояние кабины лифта в машине состояний.
 * <p>
 * Типичные переходы:
 * {@link #IDLE} → {@link #MOVING} → {@link #OPENING_DOORS} → {@link #WAITING}
 * → снова {@link #MOVING} или {@link #IDLE}.
 */

public enum CabinState {
    /** Кабина стоит, активных целей нет. */
    IDLE("Стоит"),
    /** Кабина движется между этажами. */
    MOVING("Едет"),
    /** Кабина открывает двери. */
    OPENING_DOORS("Открывает двери"),
    /** Кабина ждёт посадки или высадки пассажиров. */
    WAITING("Ждёт посадки");

    /**
     * Возвращает человекочитаемое описание состояния.
     *
     * @return описание на русском языке
     */

    private final String description;

    CabinState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return description;
    }
}