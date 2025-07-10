package com.example.habitleague.achievement.service;

import com.example.habitleague.achievement.dto.AchievementResponse;
import com.example.habitleague.achievement.dto.AchievementStatsResponse;
import com.example.habitleague.achievement.dto.UserAchievementResponse;
import com.example.habitleague.achievement.model.Achievement;
import com.example.habitleague.achievement.model.UserAchievement;
import com.example.habitleague.achievement.repository.AchievementRepository;
import com.example.habitleague.achievement.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones básicas de logros
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AchievementService {
    
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    
    /**
     * Obtiene todos los logros disponibles
     */
    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievements() {
        log.debug("Obteniendo todos los logros disponibles");
        List<Achievement> achievements = achievementRepository.findByIsActiveTrue();
        return achievements.stream()
            .map(AchievementResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene los logros desbloqueados por un usuario
     */
    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getUserAchievements(Long userId) {
        log.debug("Obteniendo logros para usuario: {}", userId);
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserIdWithAchievement(userId);
        return userAchievements.stream()
            .map(UserAchievementResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene estadísticas de logros para un usuario
     */
    @Transactional(readOnly = true)
    public AchievementStatsResponse getUserStats(Long userId) {
        log.debug("Obteniendo estadísticas de logros para usuario: {}", userId);
        
        // Obtener totales
        Long totalAchievements = achievementRepository.countActiveAchievements();
        Long unlockedAchievements = userAchievementRepository.countByUserId(userId);
        
        // Obtener logros recientes
        List<UserAchievement> recentAchievements = userAchievementRepository.findRecentByUserId(userId);
        List<UserAchievementResponse> recentAchievementResponses = recentAchievements.stream()
            .map(UserAchievementResponse::fromEntity)
            .collect(Collectors.toList());
        
        // Obtener fecha del último logro desbloqueado
        var lastUnlockedAt = recentAchievements.isEmpty() ? null : recentAchievements.get(0).getUnlockedAt();
        
        // Crear estadísticas
        AchievementStatsResponse stats = AchievementStatsResponse.builder()
            .userId(userId)
            .totalAchievements(totalAchievements)
            .unlockedAchievements(unlockedAchievements)
            .lastUnlockedAt(lastUnlockedAt)
            .recentAchievements(recentAchievementResponses)
            .build();
        
        // Calcular porcentaje de completado
        stats.calculateCompletionPercentage();
        
        log.info("Estadísticas de logros para usuario {}: {}/{} logros ({}%)", 
            userId, unlockedAchievements, totalAchievements, stats.getCompletionPercentage());
        
        return stats;
    }
    
    /**
     * Obtiene logros por challenge ID
     */
    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getAchievementsByChallenge(Long challengeId) {
        log.debug("Obteniendo logros para challenge: {}", challengeId);
        List<UserAchievement> achievements = userAchievementRepository.findByChallengeId(challengeId);
        return achievements.stream()
            .map(UserAchievementResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica si un usuario tiene un logro específico
     */
    @Transactional(readOnly = true)
    public boolean hasUserAchievement(Long userId, com.example.habitleague.achievement.model.AchievementType achievementType) {
        return userAchievementRepository.existsByUserIdAndAchievementType(userId, achievementType);
    }
} 