package com.elevator.model;

public final class SimulationClock {

    private long currentTime;

    public SimulationClock() {
        this(0L);
    }

    public SimulationClock(long initialTime) {
        if (initialTime < 0) {
            throw new IllegalArgumentException("initialTime must be non-negative");
        }
        this.currentTime = initialTime;
    }

    public long now() {
        return currentTime;
    }

    public void advance(long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must be non-negative: " + delta);
        }
        currentTime += delta;
    }

    public void set(long time) {
        if (time < 0) {
            throw new IllegalArgumentException("time must be non-negative");
        }
        this.currentTime = time;
    }

    public String toString() {
        return "t=" + currentTime;
    }
}