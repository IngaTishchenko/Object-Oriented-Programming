# Симуляция лифта (Elevator Simulation)

Модель предметной области «Симуляция лифта» на Java 21. Базовый уровень.

## Модель

- **Building** — здание с диапазоном этажей `[minFloor, maxFloor]`.
- **Elevator** — кабина: id, текущий этаж, состояние (`CabinState`), направление (`Direction`).
- **FloorRequest** — заявка (record): этаж, направление, момент нажатия.
- **SimulationClock** — модельные часы (время двигается событиями).
- **EventLog** — журнал событий с временными метками.
- **Metrics** — среднее/макс. время ожидания, число остановок.
- **ElevatorController** — контроллер: принимает заявки, управляет машиной состояний кабины, использует стратегию выбора цели.
- **MovementStrategy** — интерфейс стратегии (полиморфный метод выбора цели).
- **CollectingStrategy** — реализация: сбор попутных заявок (алгоритм базового уровня).

### Машина состояний кабины

```
IDLE → MOVING → OPENING_DOORS → WAITING → (MOVING | IDLE)
```

Длительности (тики):
- переезд на 1 этаж: 5
- открытие дверей: 2
- посадка: 3

### Исключение

`InvalidFloorException` — заявка на несуществующий этаж.

### Требования Java 21

- `record` для `FloorRequest` и `EventLog.Entry`
- `switch` как выражение / pattern matching в контроллере
- `var` для локальных переменных
- enum с полями и методами (`CabinState`, `Direction`)

## Требования

- **JDK 21** (не JRE) — https://adoptium.net/ или Oracle/Microsoft OpenJDK
- Maven **не обязателен**: в проекте есть Maven Wrapper (`mvnw` / `mvnw.cmd`)

Проверка Java:

```text
java -version
javac -version
```

Оба должны показывать 21.x.

## Сборка и запуск

### Windows (PowerShell / cmd) — без установки Maven

```powershell
# в папке elevator-sim
.\mvnw.cmd clean package
.\mvnw.cmd test
.\mvnw.cmd exec:java "-Dexec.mainClass=com.elevator.Demo"
```

Или после `package`:

```powershell
java -Dfile.encoding=UTF-8 -jar target\elevator-sim-1.0-SNAPSHOT.jar
```

### Linux / macOS

```bash
./mvnw clean package
./mvnw test
./mvnw exec:java -Dexec.mainClass="com.elevator.Demo"
# или
java -Dfile.encoding=UTF-8 -jar target/elevator-sim-1.0-SNAPSHOT.jar
```

### Если Maven уже установлен глобально

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="com.elevator.Demo"
mvn test
mvn javadoc:javadoc
```

## Структура пакетов

```
com.elevator
├── Demo                    — консольная демонстрация
├── model                   — Building, Elevator, FloorRequest, CabinState,
│                             Direction, SimulationClock, EventLog, Metrics
├── controller              — ElevatorController, MovementStrategy,
│                             CollectingStrategy
└── exception               — InvalidFloorException
```

## Демонстрация

`Demo` создаёт здание (1–10), кабину, ≥ 35 заявок, запускает симуляцию со стратегией «попутный сбор», выводит метрики и журнал событий.
