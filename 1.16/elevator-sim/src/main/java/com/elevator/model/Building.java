package com.elevator.model;

import com.elevator.exception.InvalidFloorException;

public final class Building {

    private final int minFloor;
    private final int maxFloor;

    public Building(int minFloor, int maxFloor) {
        if (maxFloor < minFloor) {
            throw new IllegalArgumentException(
                    "maxFloor (" + maxFloor + ") must be >= minFloor (" + minFloor + ")");
        }
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    public int getMinFloor() {
        return minFloor;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    public int floorCount() {
        return maxFloor - minFloor + 1;
    }

    public boolean isValidFloor(int floor) {
        return floor >= minFloor && floor <= maxFloor;
    }

    public void requireValidFloor(int floor) {
        if (!isValidFloor(floor)) {
            throw new InvalidFloorException(floor, minFloor, maxFloor);
        }
    }

    public String toString() {
        return String.format("Building[%d..%d]", minFloor, maxFloor);
    }
}