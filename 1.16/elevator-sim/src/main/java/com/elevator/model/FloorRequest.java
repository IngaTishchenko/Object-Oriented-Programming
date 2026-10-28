package com.elevator.model;

import java.util.Objects;

/**
 * Заявка на вызов лифта: этаж, направление и момент нажатия кнопки.
 * <p>
 * Неизменяемый value-object ({@code record}).
 * Равенство ({@code equals}/{@code hashCode}) определяется всеми полями.
 *
 * @param floor     номер этажа вызова
 * @param direction желаемое направление движения
 * @param time      модельное время нажатия (тики), не отрицательное
 */

public record FloorRequest(int floor, Direction direction, long time) {

    /**
     * Проверяет корректность полей при создании заявки.
     *
     * @throws NullPointerException     если {@code direction == null}
     * @throws IllegalArgumentException если {@code time < 0}
     */

    public FloorRequest {
        Objects.requireNonNull(direction, "direction must not be null");
        if (time < 0) {
            throw new IllegalArgumentException("time must be non-negative: " + time);
        }
    }

    /**
     * Создаёт заявку с временем {@code 0}.
     *
     * @param floor     номер этажа
     * @param direction направление
     * @return новая заявка
     */

    public static FloorRequest of(int floor, Direction direction) {
        return new FloorRequest(floor, direction, 0L);
    }
}