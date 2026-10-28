package com.elevator;

import com.elevator.controller.CollectingStrategy;
import com.elevator.controller.ElevatorController;
import com.elevator.controller.MovementStrategy;

import com.elevator.exception.InvalidFloorException;
import com.elevator.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class ElevatorModelTest {

    private Building building;
    private Elevator elevator;
    private SimulationClock clock;
    private EventLog log;
    private Metrics metrics;
    private MovementStrategy strategy;
    private ElevatorController controller;

    @BeforeEach
    void setUp() {
        building = new Building(1, 10);
        elevator = new Elevator("T1", 1);
        clock = new SimulationClock();
        log = new EventLog();
        metrics = new Metrics();
        strategy = new CollectingStrategy();
        controller = new ElevatorController(building, elevator, clock, strategy, log, metrics);
    }


    @Test
    @DisplayName("Building: валидный диапазон этажей")
    void buildingValidRange() {
        assertEquals(1, building.getMinFloor());
        assertEquals(10, building.getMaxFloor());
        assertEquals(10, building.floorCount());
        assertTrue(building.isValidFloor(1));
        assertTrue(building.isValidFloor(10));
        assertFalse(building.isValidFloor(0));
        assertFalse(building.isValidFloor(11));
    }

    @Test
    @DisplayName("Building: некорректный конструктор бросает IllegalArgumentException")
    void buildingInvalidConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Building(5, 3));
    }


    @Test
    @DisplayName("addRequest на несуществующий этаж → InvalidFloorException")
    void addRequestInvalidFloor() {
        InvalidFloorException ex = assertThrows(InvalidFloorException.class,
                () -> controller.addRequest(99, Direction.UP));
        assertEquals(99, ex.getFloor());
        assertEquals(1, ex.getMinFloor());
        assertEquals(10, ex.getMaxFloor());
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    @DisplayName("requireValidFloor на граничных значениях")
    void requireValidFloorBoundaries() {
        assertDoesNotThrow(() -> building.requireValidFloor(1));
        assertDoesNotThrow(() -> building.requireValidFloor(10));
        assertThrows(InvalidFloorException.class, () -> building.requireValidFloor(0));
        assertThrows(InvalidFloorException.class, () -> building.requireValidFloor(11));
    }


    @Test
    @DisplayName("FloorRequest: equals/hashCode контракт")
    void floorRequestEqualsHashCode() {
        FloorRequest a = new FloorRequest(5, Direction.UP, 10);
        FloorRequest b = new FloorRequest(5, Direction.UP, 10);
        FloorRequest c = new FloorRequest(5, Direction.DOWN, 10);
        FloorRequest d = new FloorRequest(6, Direction.UP, 10);
        FloorRequest e = new FloorRequest(5, Direction.UP, 11);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, e);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    @DisplayName("FloorRequest: отрицательное время → IllegalArgumentException")
    void floorRequestNegativeTime() {
        assertThrows(IllegalArgumentException.class,
                () -> new FloorRequest(1, Direction.UP, -1));
    }


    @Test
    @DisplayName("Elevator: equals/hashCode по id")
    void elevatorEqualsHashCode() {
        Elevator a = new Elevator("X", 1);
        Elevator b = new Elevator("X", 5); // другой этаж — всё равно equal
        Elevator c = new Elevator("Y", 1);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
    }

    @Test
    @DisplayName("Elevator: пустой id → IllegalArgumentException")
    void elevatorBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new Elevator("", 1));
        assertThrows(IllegalArgumentException.class, () -> new Elevator("  ", 1));
        assertThrows(NullPointerException.class, () -> new Elevator(null, 1));
    }


    @Test
    @DisplayName("SimulationClock: advance и now")
    void clockAdvance() {
        assertEquals(0, clock.now());
        clock.advance(10);
        assertEquals(10, clock.now());
        clock.advance(0);
        assertEquals(10, clock.now());
        assertThrows(IllegalArgumentException.class, () -> clock.advance(-1));
    }


    @Test
    @DisplayName("Metrics: average, max, stops")
    void metricsBasic() {
        assertEquals(0.0, metrics.averageWaitingTime());
        assertEquals(0, metrics.maxWaitingTime());
        assertEquals(0, metrics.getStopCount());

        metrics.recordWaitingTime(10);
        metrics.recordWaitingTime(20);
        metrics.recordWaitingTime(30);
        metrics.recordStop();
        metrics.recordStop();

        assertEquals(20.0, metrics.averageWaitingTime());
        assertEquals(30, metrics.maxWaitingTime());
        assertEquals(2, metrics.getStopCount());
        assertEquals(3, metrics.getServedRequestCount());
    }

    @Test
    @DisplayName("Metrics: отрицательное время ожидания → исключение")
    void metricsNegativeWait() {
        assertThrows(IllegalArgumentException.class, () -> metrics.recordWaitingTime(-1));
    }

    // ---------- 7. Strategy polymorphism ----------

    @Test
    @DisplayName("CollectingStrategy выбирает попутную цель")
    void collectingStrategyChoosesAlong() {
        var requests = List.of(
                new FloorRequest(3, Direction.UP, 0),
                new FloorRequest(7, Direction.UP, 1),
                new FloorRequest(2, Direction.DOWN, 2)
        );
        Optional<Integer> next = strategy.chooseNextTarget(1, Direction.UP, requests);
        assertTrue(next.isPresent());
        assertEquals(2, next.get()); // ближайший этаж по пути вверх
    }



    @Test
    @DisplayName("Контроллер обслуживает одну заявку")
    void controllerServesSingleRequest() {
        controller.addRequest(5, Direction.UP);
        assertEquals(1, controller.pendingCount());

        int steps = controller.runUntilIdle(1000);
        assertTrue(steps > 0);
        assertEquals(0, controller.pendingCount());
        assertEquals(CabinState.IDLE, elevator.getState());
        assertEquals(1, metrics.getServedRequestCount());
        assertTrue(metrics.getStopCount() >= 1);
        assertTrue(log.size() > 0);
    }

    @Test
    @DisplayName("Контроллер собирает попутные заявки")
    void controllerCollectsAlongTheWay() {
        controller.addRequest(3, Direction.UP);
        controller.addRequest(7, Direction.UP);

        controller.runUntilIdle(2000);

        assertEquals(0, controller.pendingCount());
        assertEquals(2, metrics.getServedRequestCount());
        assertTrue(metrics.getStopCount() <= 3);
    }

    @Test
    @DisplayName("Заявка DOWN с 1 этажа → IllegalArgumentException")
    void requestDownFromBottom() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.addRequest(1, Direction.DOWN));
    }

    @Test
    @DisplayName("Заявка UP с 10 этажа → IllegalArgumentException")
    void requestUpFromTop() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.addRequest(10, Direction.UP));
    }


    @Test
    @DisplayName("EventLog: getEntries возвращает immutable snapshot")
    void eventLogImmutable() {
        log.log(0, "test");
        var entries = log.getEntries();
        assertEquals(1, entries.size());
        assertThrows(UnsupportedOperationException.class, () -> entries.add(new EventLog.Entry(1, "x")));
    }


    @Test
    @DisplayName("Direction.opposite")
    void directionOpposite() {
        assertEquals(Direction.DOWN, Direction.UP.opposite());
        assertEquals(Direction.UP, Direction.DOWN.opposite());
    }


    @Test
    @DisplayName("CabinState имеет описание")
    void cabinStateDescription() {
        assertEquals("Стоит", CabinState.IDLE.getDescription());
        assertEquals("Едет", CabinState.MOVING.getDescription());
    }


    @Test
    @DisplayName("Metrics equals/hashCode")
    void metricsEquals() {
        Metrics m1 = new Metrics();
        Metrics m2 = new Metrics();
        m1.recordWaitingTime(5);
        m2.recordWaitingTime(5);
        m1.recordStop();
        m2.recordStop();
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }
}