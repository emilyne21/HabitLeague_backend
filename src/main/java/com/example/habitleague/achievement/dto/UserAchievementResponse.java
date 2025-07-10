package com.example.habitleague.achievement.dto;

import com.example.habitleague.achievement.model.UserAchievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para representar un logro desbloqueado por un usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementResponse {
    
    private Long id;
    private Long userId;
    private AchievementResponse achievement;
    private LocalDateTime unlockedAt;
    private Long challengeId;
    private String contextInfo;
    
    /**
     * Convierte una entidad UserAchievement a UserAchievementResponse
     */
    public static UserAchievementResponse fromEntity(UserAchievement userAchievement) {
        return UserAchievementResponse.builder()
            .id(userAchievement.getId())
            .userId(userAchievement.getUser().getId())
            .achievement(AchievementResponse.fromEntity(userAchievement.getAchievement()))
            .unlockedAt(userAchievement.getUnlockedAt())
            .challengeId(userAchievement.getChallengeId())
            .contextInfo(userAchievement.getContextInfo())
            .build();
    }
} 