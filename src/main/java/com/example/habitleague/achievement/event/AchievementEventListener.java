package com.example.habitleague.achievement.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos relacionados con logros
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AchievementEventListener {
    
    /**
     * Maneja el evento de logro desbloqueado
     */
    @EventListener
    @Async
    public void handleAchievementUnlocked(AchievementUnlockedEvent event) {
        log.info("🏆 Usuario {} desbloqueó logro: {} [{}]", 
            event.getUserId(), 
            event.getAchievementName(), 
            event.getAchievementType());
        
        // Aquí se pueden agregar diferentes tipos de notificaciones
        try {
            // 1. Registro en log para auditoría
            logAchievementUnlocked(event);
            
            // 2. Envío de notificación push (simulado)
            sendPushNotification(event);
            
            // 3. Registro en analytics (simulado)
            trackAchievementAnalytics(event);
            
        } catch (Exception e) {
            log.error("Error procesando evento de logro desbloqueado: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Registra el logro desbloqueado en el log para auditoría
     */
    private void logAchievementUnlocked(AchievementUnlockedEvent event) {
        log.info("ACHIEVEMENT_UNLOCKED | UserId: {} | AchievementType: {} | ChallengeId: {} | UnlockedAt: {}", 
            event.getUserId(),
            event.getAchievementType(),
            event.getChallengeId(),
            event.getUnlockedAt());
    }
    
    /**
     * Simula el envío de una notificación push
     */
    private void sendPushNotification(AchievementUnlockedEvent event) {
        // En un sistema real, aquí se integraría con un servicio de notificaciones
        // como Firebase Cloud Messaging, OneSignal, etc.
        log.info("📱 Enviando notificación push a usuario {}: '¡Felicidades! Has desbloqueado: {}'", 
            event.getUserId(), 
            event.getAchievementName());
    }
    
    /**
     * Simula el registro en un sistema de analytics
     */
    private void trackAchievementAnalytics(AchievementUnlockedEvent event) {
        // En un sistema real, aquí se enviarían eventos a Google Analytics, 
        // Mixpanel, Amplitude, etc.
        log.info("📊 Registrando en analytics: achievement_unlocked | user_id: {} | achievement_type: {}", 
            event.getUserId(), 
            event.getAchievementType());
    }
} 