package com.example.habitleague.achievement.model;

/**
 * Enum que define los tipos de logros disponibles en el sistema
 */
public enum AchievementType {
    FIRST_CHALLENGE_COMPLETED("Primer reto completado"),
    SEVEN_DAY_STREAK("Racha de 7 días"),
    PERFECT_CHALLENGE("Sin excusas"),
    FIRST_PENALTY_PAYMENT("Primer pago de penalización");

    private final String displayName;

    AchievementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 