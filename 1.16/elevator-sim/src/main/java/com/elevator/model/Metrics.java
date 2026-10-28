package com.elevator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Metrics {

    private final List<Long> waitingTimes = new ArrayList<>();
    private int stopCount;

    public void recordWaitingTime(long waitingTime) {
        if (waitingTime < 0) {
            throw new IllegalArgumentException("waitingTime must be non-negative: " + waitingTime);
        }
        waitingTimes.add(waitingTime);
    }

    public void recordStop() {
        stopCount++;
    }

    public double averageWaitingTime() {
        if (waitingTimes.isEmpty()) {
            return 0.0;
        }
        long sum = 0;
        for (long t : waitingTimes) {
            sum += t;
        }
        return (double) sum / waitingTimes.size();
    }

    public long maxWaitingTime() {
        if (waitingTimes.isEmpty()) {
            return 0L;
        }
        long max = 0;
        for (long t : waitingTimes) {
            if (t > max) {
                max = t;
            }
        }
        return max;
    }

    public int getStopCount() {
        return stopCount;
    }

    public int getServedRequestCount() {
        return waitingTimes.size();
    }

    public List<Long> getWaitingTimes() {
        return Collections.unmodifiableList(new ArrayList<>(waitingTimes));
    }

    public void reset() {
        waitingTimes.clear();
        stopCount = 0;
    }

    public String toString() {
        return String.format(
                "Metrics{served=%d, stops=%d, avgWait=%.2f, maxWait=%d}",
                getServedRequestCount(), stopCount, averageWaitingTime(), maxWaitingTime());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metrics metrics)) return false;
        return stopCount == metrics.stopCount && waitingTimes.equals(metrics.waitingTimes);
    }

    public int hashCode() {
        return Objects.hash(waitingTimes, stopCount);
    }
}