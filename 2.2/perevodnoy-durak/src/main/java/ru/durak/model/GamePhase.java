package ru.durak.model;

/**
 * Фаза текущего кона.
 */
public enum GamePhase {
    /** Ожидание первой атаки */
    WAITING_ATTACK("Ожидание атаки"),
    /** Защита / перевод / взятие */
    DEFENDING("Защита"),
    /** Подкидывание после начала отбоя */
    THROWING("Подкидывание"),
    /** Игра окончена */
    FINISHED("Игра окончена");

    private final String displayName;

    GamePhase(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Название фазы на русском.
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
