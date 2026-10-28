package ru.durak.console;

import ru.durak.game.*;
import ru.durak.model.*;

import java.util.*;

/**
 * Консольное приложение.
 * Можно запускать партию человек+боты или только боты.
 */
public class Main {

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.currentTimeMillis();
        Random random = new Random(seed);
        System.out.println("Seed: " + seed);

        Scanner scanner = new Scanner(System.in);
        int n = 2;
        while (true) {
            System.out.print("Количество игроков (2-4): ");
            try {
                n = Integer.parseInt(scanner.nextLine().trim());
                if (n >= 2 && n <= 4) break;
                System.out.println("Введите число от 2 до 4.");
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести число от 2 до 4.");
            }
        }

        List<String> names = new ArrayList<>();
        Map<String, PlayerStrategy> strategies = new HashMap<>();

        System.out.print("Вы человек? (y/n): ");
        boolean human = scanner.nextLine().trim().toLowerCase().startsWith("y");

        names.add(human ? "Вы" : "Бот-1");
        for (int i = 2; i <= n; i++) {
            names.add("Бот-" + i);
        }

        GameEngine engine = GameEngine.createNewGame(names, random);
        GameState state = engine.getState();

        if (human) {
            strategies.put("Вы", new HumanStrategy(scanner));
        }
        for (String name : names) {
            if (!strategies.containsKey(name)) {
                strategies.put(name, new GreedyStrategy(state.getTrump()));
            }
        }

        ConsoleView view = new ConsoleView();
        view.printEvents(engine.getLastEvents());
        view.printState(state);

        int safety = 0;
        while (!state.isGameOver() && safety++ < 5000) {
            boolean moved = playOneStep(engine, state, strategies, view);
            if (!moved) {
                break;
            }
        }

        if (state.getFool() != null) {
            System.out.println("Игра окончена. Дурак: " + state.getFool());
        } else {
            System.out.println("Игра окончена.");
        }
    }

    /**
     * Один шаг игрового цикла.
     * @return false, если ходов больше нет
     */
    private static boolean playOneStep(GameEngine engine, GameState state,
                                       Map<String, PlayerStrategy> strategies, ConsoleView view) {
        GamePhase phase = state.getPhase();

        if (phase == GamePhase.WAITING_ATTACK) {
            return applyChosen(engine, state, state.getAttacker(), strategies, view);
        }

        if (phase == GamePhase.DEFENDING || phase == GamePhase.THROWING) {
            // 1) Сначала даём возможность подкинуть всем, кроме защищающегося
            for (Player p : state.getPlayers()) {
                if (p == state.getDefender() || p.isFinished()) continue;
                List<Move> throwsOnly = engine.getLegalMoves(p).stream()
                        .filter(m -> m.getType() == MoveType.THROW_IN)
                        .toList();
                if (throwsOnly.isEmpty()) continue;

                PlayerStrategy strategy = strategies.get(p.getName());
                // боты подкидывают жадно; человек — только если он не защищается (редко)
                if (strategy instanceof HumanStrategy) continue;

                Move chosen = strategy.chooseMove(p, state, throwsOnly);
                if (chosen != null) {
                    try {
                        view.printEvents(engine.apply(chosen));
                        view.printState(state);
                        return true;
                    } catch (IllegalMoveException ignored) {
                        // пробуем следующего
                    }
                }
            }

            // 2) Все отбиты и никто не подкинул → успешная защита
            if (state.unbeatenCount() == 0) {
                try {
                    view.printEvents(engine.confirmSuccessfulDefense());
                    view.printState(state);
                    return true;
                } catch (IllegalMoveException e) {
                    System.out.println("Ошибка завершения кона: " + e.getMessage());
                    return false;
                }
            }

            // 3) Ход защищающегося (бить / перевести / взять)
            return applyChosen(engine, state, state.getDefender(), strategies, view);
        }

        return false;
    }

    private static boolean applyChosen(GameEngine engine, GameState state, Player current,
                                       Map<String, PlayerStrategy> strategies, ConsoleView view) {
        List<Move> legal = engine.getLegalMoves(current);
        if (legal.isEmpty()) {
            return false;
        }
        PlayerStrategy strategy = strategies.get(current.getName());
        Move move = strategy.chooseMove(current, state, legal);
        if (move == null) {
            return false;
        }
        try {
            view.printEvents(engine.apply(move));
            view.printState(state);
            return true;
        } catch (IllegalMoveException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return true; // продолжаем, чтобы не зависнуть
        }
    }

    /**
     * Стратегия человека: выбор только из предложенных номеров.
     */
    static class HumanStrategy implements PlayerStrategy {
        private final Scanner scanner;

        HumanStrategy(Scanner scanner) {
            this.scanner = scanner;
        }

        @Override
        public Move chooseMove(Player player, GameState state, List<Move> legal) {
            if (legal.isEmpty()) {
                return null;
            }
            System.out.println("\nВаш ход. Возможные действия:");
            for (int i = 0; i < legal.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + legal.get(i));
            }
            while (true) {
                System.out.print("Выберите номер (1–" + legal.size() + "): ");
                String line = scanner.nextLine().trim();
                try {
                    int idx = Integer.parseInt(line) - 1;
                    if (idx >= 0 && idx < legal.size()) {
                        return legal.get(idx);
                    }
                    System.out.println("Нет такого варианта. Введите число от 1 до " + legal.size() + ".");
                } catch (NumberFormatException e) {
                    System.out.println("Нужно ввести номер (число от 1 до " + legal.size() + ").");
                }
            }
        }
    }
}
