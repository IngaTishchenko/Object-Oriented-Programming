package com.elevator.exception;

/**
 * Исключение при обращении к этажу, которого нет в здании.
 */


public class InvalidFloorException extends RuntimeException {

    private final int floor;
    private final int minFloor;
    private final int maxFloor;

    /**
     * Создаёт исключение с указанием запрошенного этажа и допустимого диапазона.
     *
     * @param floor    запрошенный (несуществующий) этаж
     * @param minFloor минимальный этаж здания
     * @param maxFloor максимальный этаж здания
     */

    public InvalidFloorException(int floor, int minFloor, int maxFloor) {
        super(String.format(
                "Этаж %d не существует. Допустимый диапазон: [%d, %d]",
                floor, minFloor, maxFloor));
        this.floor = floor;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    /**
     * @return запрошенный этаж
     */

    public int getFloor() {
        return floor;
    }

    /**
     * @return минимальный этаж здания
     */

    public int getMinFloor() {
        return minFloor;
    }

    /**
     * @return максимальный этаж здания
     */

    public int getMaxFloor() {
        return maxFloor;
    }
}