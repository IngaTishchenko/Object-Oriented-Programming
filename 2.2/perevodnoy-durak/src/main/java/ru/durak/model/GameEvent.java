package ru.durak.model;

import java.util.List;

/**
 * Событие, произошедшее в результате хода.
 * Механика возвращает события, а не печатает в консоль.
 */
public sealed interface GameEvent {

    record GameStarted(List<String> playerNames, Suit trump, Card trumpCard) implements GameEvent {}

    record CardsDealt(String playerName, int count) implements GameEvent {}

    record AttackStarted(String attacker, List<Card> cards) implements GameEvent {}

    record CardDefended(String defender, Card attack, Card defense) implements GameEvent {}

    record Transferred(String from, String to, Card card, List<Card> allOnTable) implements GameEvent {}

    record ThrownIn(String player, List<Card> cards) implements GameEvent {}

    record Taken(String player, List<Card> cards) implements GameEvent {}

    record RoundWon(String defender) implements GameEvent {}

    record PlayerFinished(String player) implements GameEvent {}

    record GameOver(String fool) implements GameEvent {}

    record CardsDrawn(String player, int count) implements GameEvent {}

    record IllegalMove(String reason) implements GameEvent {}
}
