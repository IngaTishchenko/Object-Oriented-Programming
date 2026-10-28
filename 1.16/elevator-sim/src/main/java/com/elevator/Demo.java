package com.elevator;

import com.elevator.controller.CollectingStrategy;
import com.elevator.controller.ElevatorController;
import com.elevator.controller.MovementStrategy;
import com.elevator.exception.InvalidFloorException;
import com.elevator.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Demo {

    public static void main(String[] args) {
        System.out.println("=== Симуляция лифта (базовый уровень) ===\n");

        Building building = new Building(1, 10);
        System.out.println("1. Здание: " + building + " (" + building.floorCount() + " этажей)");

        Elevator elevator = new Elevator("A1", 1);
        System.out.println("2. Кабина: " + elevator);

        SimulationClock clock = new SimulationClock();
        System.out.println("3. Модельные часы: " + clock);

        EventLog log = new EventLog();
        Metrics metrics = new Metrics();

        MovementStrategy strategy = new CollectingStrategy();
        System.out.println("4. Стратегия: " + strategy.name());

        ElevatorController controller = new ElevatorController(
                building, elevator, clock, strategy, log, metrics);
        System.out.println("5. Контроллер создан\n");

        System.out.println("--- Проверка исключения InvalidFloorException ---");
        try {
            controller.addRequest(99, Direction.UP);
        } catch (InvalidFloorException e) {
            System.out.println("Поймано: " + e.getMessage());
            System.out.println("  floor=" + e.getFloor() + ", range=[" + e.getMinFloor() + "," + e.getMaxFloor() + "]");
        }
        System.out.println();

        System.out.println("--- Добавление 35 заявок ---");
        List<FloorRequest> generated = generateRequests(35, building, clock);
        for (FloorRequest r : generated) {
            clock.advance(1);
            try {
                controller.addRequest(r.floor(), r.direction());
            } catch (IllegalArgumentException ex) {
                System.out.println("  пропущена некорректная заявка: " + r + " (" + ex.getMessage() + ")");
            }
        }
        System.out.println("Добавлено заявок в очередь: " + controller.pendingCount());
        System.out.println("Всего объектов FloorRequest создано: " + generated.size());
        System.out.println();

        System.out.println("--- Запуск симуляции (сбор попутных заявок) ---");
        int steps = controller.runUntilIdle(10_000);
        System.out.println("Выполнено шагов: " + steps);
        System.out.println("Финальное состояние кабины: " + elevator);
        System.out.println("Время симуляции: " + clock.now() + " тиков");
        System.out.println();

        System.out.println("--- Метрики ---");
        System.out.println(metrics);
        System.out.printf("  Среднее время ожидания: %.2f тиков%n", metrics.averageWaitingTime());
        System.out.println("  Максимальное время ожидания: " + metrics.maxWaitingTime() + " тиков");
        System.out.println("  Число остановок: " + metrics.getStopCount());
        System.out.println("  Обслужено заявок: " + metrics.getServedRequestCount());
        System.out.println();

        System.out.println("--- Журнал событий (всего " + log.size() + ") ---");
        var entries = log.getEntries();
        int show = Math.min(15, entries.size());
        System.out.println("Первые " + show + ":");
        for (int i = 0; i < show; i++) {
            System.out.println("  " + entries.get(i));
        }
        if (entries.size() > 30) {
            System.out.println("  ...");
            System.out.println("Последние 10:");
            for (int i = entries.size() - 10; i < entries.size(); i++) {
                System.out.println("  " + entries.get(i));
            }
        }
        System.out.println();
        System.out.println("=== Демонстрация завершена ===");
    }

    private static List<FloorRequest> generateRequests(int count, Building building, SimulationClock clock) {
        Random rnd = new Random(42);
        List<FloorRequest> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int floor = building.getMinFloor() + rnd.nextInt(building.floorCount());
            Direction dir;
            if (floor == building.getMaxFloor()) {
                dir = Direction.DOWN;
            } else if (floor == building.getMinFloor()) {
                dir = Direction.UP;
            } else {
                dir = rnd.nextBoolean() ? Direction.UP : Direction.DOWN;
            }
            list.add(new FloorRequest(floor, dir, clock.now() + i));
        }
        return list;
    }
}