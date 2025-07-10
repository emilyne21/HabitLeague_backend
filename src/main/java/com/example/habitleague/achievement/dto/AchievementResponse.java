package com.example.habitleague.achievement.dto;

import com.example.habitleague.achievement.model.Achievement;
import com.example.habitleague.achievement.model.AchievementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para representar un logro en las respuestas de la API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    
    private Long id;
    private AchievementType type;
    private String name;
    private String description;
    private String iconUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    /**
     * Convierte una entidad Achievement a AchievementResponse
     */
    public static AchievementResponse fromEntity(Achievement achievement) {
        return AchievementResponse.builder()
            .id(achievement.getId())
            .type(achievement.getType())
            .name(achievement.getName())
            .description(achievement.getDescription())
            .iconUrl(achievement.getIconUrl())
            .isActive(achievement.getIsActive())
            .createdAt(achievement.getCreatedAt())
            .build();
    }
} 