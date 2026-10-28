package ru.durak.console;

import ru.durak.model.*;

import java.util.List;

/**
 * Слой вывода. Механика ничего не печатает — только этот класс.
 */
public class ConsoleView {

    public void printEvent(GameEvent event) {
        if (event instanceof GameEvent.GameStarted e) {
            System.out.println("=== Новая партия ===");
            System.out.println("Игроки: " + e.playerNames());
            System.out.println("Козырь: " + e.trump() + " (" + e.trumpCard() + ")");
        } else if (event instanceof GameEvent.CardsDealt e) {
            System.out.println(e.playerName() + " получил " + e.count() + " карт");
        } else if (event instanceof GameEvent.AttackStarted e) {
            System.out.println(e.attacker() + " ходит: " + e.cards());
        } else if (event instanceof GameEvent.CardDefended e) {
            System.out.println(e.defender() + " бьёт " + e.attack() + " → " + e.defense());
        } else if (event instanceof GameEvent.Transferred e) {
            System.out.println(e.from() + " переводит " + e.card() + " на " + e.to()
                    + ". На столе: " + e.allOnTable());
        } else if (event instanceof GameEvent.ThrownIn e) {
            System.out.println(e.player() + " подкидывает " + e.cards());
        } else if (event instanceof GameEvent.Taken e) {
            System.out.println(e.player() + " берёт " + e.cards());
        } else if (event instanceof GameEvent.RoundWon e) {
            System.out.println(e.defender() + " отбился успешно");
        } else if (event instanceof GameEvent.PlayerFinished e) {
            System.out.println("*** " + e.player() + " вышел из игры ***");
        } else if (event instanceof GameEvent.GameOver e) {
            if (e.fool() == null) {
                System.out.println("=== Ничья ===");
            } else {
                System.out.println("=== Дурак: " + e.fool() + " ===");
            }
        } else if (event instanceof GameEvent.CardsDrawn e) {
            System.out.println(e.player() + " добрал " + e.count() + " карт(у)");
        } else if (event instanceof GameEvent.IllegalMove e) {
            System.out.println("Недопустимый ход: " + e.reason());
        }
    }

    public void printEvents(List<GameEvent> events) {
        for (GameEvent e : events) {
            printEvent(e);
        }
    }

    public void printState(GameState state) {
        System.out.println("-----");
        System.out.println("Козырь: " + state.getTrump() + " | Колода: " + state.getDeck().size());
        System.out.println("Стол: " + state.getTable());
        for (Player p : state.getPlayers()) {
            String role = "";
            if (p == state.getAttacker()) role = " [атакует]";
            if (p == state.getDefender()) role = " [защищается]";
            System.out.println(p.getName() + role + ": " + p.getHand());
        }
        System.out.println("Фаза: " + state.getPhase());
        System.out.println("-----");
    }
}
