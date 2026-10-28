package com.elevator.controller;

import com.elevator.model.Direction;
import com.elevator.model.FloorRequest;

import java.util.Collection;
import java.util.Optional;

public interface MovementStrategy {

    Optional<Integer> chooseNextTarget(
            int currentFloor,
            Direction currentDirection,
            Collection<FloorRequest> pendingRequests);

    String name();
}