package com.example.habitleague.achievement.event;

import com.example.habitleague.achievement.model.AchievementType;
import com.example.habitleague.achievement.model.UserAchievement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Evento que se dispara cuando un usuario desbloquea un logro
 */
@Getter
@AllArgsConstructor
public class AchievementUnlockedEvent {
    
    private final Long userId;
    private final Long achievementId;
    private final AchievementType achievementType;
    private final Long challengeId;
    private final String contextInfo;
    private final LocalDateTime unlockedAt;
    
    /**
     * Constructor que crea el evento a partir de una entidad UserAchievement
     */
    public static AchievementUnlockedEvent fromUserAchievement(UserAchievement userAchievement) {
        return new AchievementUnlockedEvent(
            userAchievement.getUser().getId(),
            userAchievement.getAchievement().getId(),
            userAchievement.getAchievement().getType(),
            userAchievement.getChallengeId(),
            userAchievement.getContextInfo(),
            userAchievement.getUnlockedAt()
        );
    }
    
    /**
     * Obtiene el nombre del logro para mostrar en notificaciones
     */
    public String getAchievementName() {
        return achievementType.getDisplayName();
    }
} 