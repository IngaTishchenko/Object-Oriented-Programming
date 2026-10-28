package com.elevator.model;

import java.util.Objects;

/**
 * Кабина лифта: идентификатор, текущий этаж, состояние и направление.
 * <p>
 * Идентичность задаётся полем {@code id} ({@code equals}/{@code hashCode}).
 */

public final class Elevator {

    /**
     * Создаёт кабину на указанном этаже в состоянии {@link CabinState#IDLE}.
     *
     * @param id           идентификатор кабины (не пустой)
     * @param initialFloor начальный этаж
     * @throws NullPointerException     если {@code id == null}
     * @throws IllegalArgumentException если {@code id} пустой или из пробелов
     */

    private final String id;
    private int currentFloor;
    private CabinState state;
    private Direction direction;

    public Elevator(String id, int initialFloor) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.id = id;
        this.currentFloor = initialFloor;
        this.state = CabinState.IDLE;
        this.direction = null;
    }

    public String getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public CabinState getState() {
        return state;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * Устанавливает состояние кабины.
     * При переходе в {@link CabinState#IDLE} направление сбрасывается в {@code null}.
     *
     * @param newState новое состояние
     * @throws NullPointerException если {@code newState == null}
     */

    public void setState(CabinState newState) {
        this.state = Objects.requireNonNull(newState);
        if (newState == CabinState.IDLE) {
            this.direction = null;
        }
    }

    /**
     * Устанавливает направление движения.
     *
     * @param direction направление; может быть {@code null}
     */

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Перемещает кабину на один этаж в текущем направлении.
     *
     * @throws IllegalStateException если состояние не {@link CabinState#MOVING}
     *                               или направление не задано
     */

    public void moveOneFloor() {
        if (state != CabinState.MOVING) {
            throw new IllegalStateException("Cannot move when state is " + state);
        }
        if (direction == null) {
            throw new IllegalStateException("Direction is not set");
        }
        currentFloor += (direction == Direction.UP ? 1 : -1);
    }

    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Elevator elevator)) return false;
        return id.equals(elevator.id);
    }

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return String.format("Elevator[%s, floor=%d, state=%s, dir=%s]",
                id, currentFloor, state, direction);
    }
}
