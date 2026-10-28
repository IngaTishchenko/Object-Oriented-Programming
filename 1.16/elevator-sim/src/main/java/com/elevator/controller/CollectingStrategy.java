package com.elevator.controller;

import com.elevator.model.Direction;
import com.elevator.model.FloorRequest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class CollectingStrategy implements MovementStrategy {

    public Optional<Integer> chooseNextTarget(
            int currentFloor,
            Direction currentDirection,
            Collection<FloorRequest> pendingRequests) {

        if (pendingRequests.isEmpty()) {
            return Optional.empty();
        }

        if (currentDirection == null) {
            return pendingRequests.stream()
                    .min(Comparator.comparingInt(r -> Math.abs(r.floor() - currentFloor)))
                    .map(FloorRequest::floor);
        }

        var sameDirection = pendingRequests.stream()
                .filter(r -> isInDirection(currentFloor, r.floor(), currentDirection))
                .toList();

        if (!sameDirection.isEmpty()) {
            Comparator<FloorRequest> byDistance = currentDirection == Direction.UP
                    ? Comparator.comparingInt(FloorRequest::floor)
                    : Comparator.comparingInt(FloorRequest::floor).reversed();
            return sameDirection.stream()
                    .min(byDistance)
                    .map(FloorRequest::floor);
        }

        return pendingRequests.stream()
                .min(Comparator.comparingInt(r -> Math.abs(r.floor() - currentFloor)))
                .map(FloorRequest::floor);
    }

    private static boolean isInDirection(int from, int to, Direction dir) {
        return dir == Direction.UP ? to > from : to < from;
    }

    public String name() {
        return "Collecting (попутный сбор)";
    }
}