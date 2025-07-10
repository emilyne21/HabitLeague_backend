package com.example.habitleague.achievement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar estadísticas de logros de un usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementStatsResponse {
    
    private Long userId;
    private Long totalAchievements;
    private Long unlockedAchievements;
    private Double completionPercentage;
    private LocalDateTime lastUnlockedAt;
    private List<UserAchievementResponse> recentAchievements;
    
    /**
     * Calcula el porcentaje de completado
     */
    public void calculateCompletionPercentage() {
        if (totalAchievements != null && totalAchievements > 0) {
            this.completionPercentage = (unlockedAchievements.doubleValue() / totalAchievements.doubleValue()) * 100.0;
        } else {
            this.completionPercentage = 0.0;
        }
    }
} 