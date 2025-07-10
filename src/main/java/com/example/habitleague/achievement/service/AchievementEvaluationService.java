package com.example.habitleague.achievement.service;

import com.example.habitleague.achievement.event.AchievementUnlockedEvent;
import com.example.habitleague.achievement.model.Achievement;
import com.example.habitleague.achievement.model.AchievementType;
import com.example.habitleague.achievement.model.UserAchievement;
import com.example.habitleague.achievement.repository.AchievementRepository;
import com.example.habitleague.achievement.repository.UserAchievementRepository;
import com.example.habitleague.user.model.User;
import com.example.habitleague.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio para evaluar y desbloquear logros basado en acciones del usuario
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AchievementEvaluationService {
    
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Evalúa y desbloquea el logro "Primer reto completado"
     */
    public void evaluateFirstChallengeCompleted(Long userId, Long challengeId) {
        log.debug("Evaluando logro 'Primer reto completado' para usuario: {}", userId);
        
        if (!hasAchievement(userId, AchievementType.FIRST_CHALLENGE_COMPLETED)) {
            String contextInfo = String.format("Primer reto completado (Challenge ID: %d)", challengeId);
            unlockAchievement(userId, AchievementType.FIRST_CHALLENGE_COMPLETED, challengeId, contextInfo);
        }
    }
    
    /**
     * Evalúa y desbloquea el logro "Racha de 7 días"
     */
    public void evaluateSevenDayStreak(Long userId, int currentStreak, Long challengeId) {
        log.debug("Evaluando logro 'Racha de 7 días' para usuario: {} (racha actual: {})", userId, currentStreak);
        
        if (currentStreak >= 7 && !hasAchievement(userId, AchievementType.SEVEN_DAY_STREAK)) {
            String contextInfo = String.format("Racha de %d días consecutivos (Challenge ID: %d)", currentStreak, challengeId);
            unlockAchievement(userId, AchievementType.SEVEN_DAY_STREAK, challengeId, contextInfo);
        }
    }
    
    /**
     * Evalúa y desbloquea el logro "Sin excusas" (reto perfecto)
     */
    public void evaluatePerfectChallenge(Long userId, Long challengeId, int progressDays, int totalDays) {
        log.debug("Evaluando logro 'Sin excusas' para usuario: {} (progreso: {}/{})", userId, progressDays, totalDays);
        
        if (progressDays == totalDays && !hasAchievement(userId, AchievementType.PERFECT_CHALLENGE)) {
            String contextInfo = String.format("Reto perfecto: %d/%d días (Challenge ID: %d)", progressDays, totalDays, challengeId);
            unlockAchievement(userId, AchievementType.PERFECT_CHALLENGE, challengeId, contextInfo);
        }
    }
    
    /**
     * Evalúa y desbloquea el logro "Primer pago de penalización"
     */
    public void evaluateFirstPenaltyPayment(Long userId, Long challengeId) {
        log.debug("Evaluando logro 'Primer pago de penalización' para usuario: {}", userId);
        
        if (!hasAchievement(userId, AchievementType.FIRST_PENALTY_PAYMENT)) {
            String contextInfo = String.format("Primer pago de penalización (Challenge ID: %d)", challengeId);
            unlockAchievement(userId, AchievementType.FIRST_PENALTY_PAYMENT, challengeId, contextInfo);
        }
    }
    
    /**
     * Verifica si un usuario ya tiene un logro específico
     */
    private boolean hasAchievement(Long userId, AchievementType achievementType) {
        return userAchievementRepository.existsByUserIdAndAchievementType(userId, achievementType);
    }
    
    /**
     * Desbloquea un logro para un usuario
     */
    private void unlockAchievement(Long userId, AchievementType achievementType, Long challengeId, String contextInfo) {
        try {
            // Buscar el logro por tipo
            Optional<Achievement> achievementOpt = achievementRepository.findByTypeAndIsActiveTrue(achievementType);
            if (achievementOpt.isEmpty()) {
                log.warn("No se encontró logro activo para tipo: {}", achievementType);
                return;
            }
            
            // Buscar el usuario
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("No se encontró usuario con ID: {}", userId);
                return;
            }
            
            Achievement achievement = achievementOpt.get();
            User user = userOpt.get();
            
            // Crear el logro desbloqueado
            UserAchievement userAchievement = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .challengeId(challengeId)
                .contextInfo(contextInfo)
                .build();
            
            // Guardar en la base de datos
            userAchievement = userAchievementRepository.save(userAchievement);
            
            // Publicar evento
            AchievementUnlockedEvent event = AchievementUnlockedEvent.fromUserAchievement(userAchievement);
            eventPublisher.publishEvent(event);
            
            log.info("✅ Logro desbloqueado: Usuario {} obtuvo '{}' [{}]", 
                userId, achievement.getName(), achievementType);
                
        } catch (Exception e) {
            log.error("Error desbloqueando logro {} para usuario {}: {}", 
                achievementType, userId, e.getMessage(), e);
        }
    }
    
    /**
     * Método público para desbloquear logros manualmente (para testing o administración)
     */
    public void unlockAchievementManually(Long userId, AchievementType achievementType, Long challengeId, String contextInfo) {
        log.info("Desbloqueando logro manualmente: {} para usuario {}", achievementType, userId);
        unlockAchievement(userId, achievementType, challengeId, contextInfo);
    }
} 