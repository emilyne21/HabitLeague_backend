package com.example.habitleague.achievement.controller;

import com.example.habitleague.achievement.dto.AchievementResponse;
import com.example.habitleague.achievement.dto.AchievementStatsResponse;
import com.example.habitleague.achievement.dto.UserAchievementResponse;
import com.example.habitleague.achievement.model.AchievementType;
import com.example.habitleague.achievement.service.AchievementEvaluationService;
import com.example.habitleague.achievement.service.AchievementService;
import com.example.habitleague.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operaciones relacionadas con logros
 */
@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Slf4j
public class AchievementController {
    
    private final AchievementService achievementService;
    private final AchievementEvaluationService achievementEvaluationService;
    
    /**
     * Obtiene todos los logros disponibles
     */
    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getAllAchievements() {
        log.info("Solicitando todos los logros disponibles");
        List<AchievementResponse> achievements = achievementService.getAllAchievements();
        return ResponseEntity.ok(achievements);
    }
    
    /**
     * Obtiene los logros desbloqueados por el usuario autenticado
     */
    @GetMapping("/my-achievements")
    public ResponseEntity<List<UserAchievementResponse>> getMyAchievements(
            @AuthenticationPrincipal User user) {
        log.info("Solicitando logros para usuario autenticado: {}", user.getEmail());
        List<UserAchievementResponse> userAchievements = achievementService.getUserAchievements(user.getId());
        return ResponseEntity.ok(userAchievements);
    }
    
    /**
     * Obtiene los logros desbloqueados por un usuario específico
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<UserAchievementResponse>> getUserAchievements(
            @PathVariable Long userId) {
        log.info("Solicitando logros para usuario: {}", userId);
        List<UserAchievementResponse> userAchievements = achievementService.getUserAchievements(userId);
        return ResponseEntity.ok(userAchievements);
    }
    
    /**
     * Obtiene estadísticas de logros para el usuario autenticado
     */
    @GetMapping("/my-stats")
    public ResponseEntity<AchievementStatsResponse> getMyAchievementStats(
            @AuthenticationPrincipal User user) {
        log.info("Solicitando estadísticas de logros para usuario autenticado: {}", user.getEmail());
        AchievementStatsResponse stats = achievementService.getUserStats(user.getId());
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Obtiene estadísticas de logros para un usuario
     */
    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<AchievementStatsResponse> getUserAchievementStats(
            @PathVariable Long userId) {
        log.info("Solicitando estadísticas de logros para usuario: {}", userId);
        AchievementStatsResponse stats = achievementService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Obtiene logros relacionados con un challenge específico
     */
    @GetMapping("/challenge/{challengeId}")
    public ResponseEntity<List<UserAchievementResponse>> getAchievementsByChallenge(
            @PathVariable Long challengeId) {
        log.info("Solicitando logros para challenge: {}", challengeId);
        List<UserAchievementResponse> achievements = achievementService.getAchievementsByChallenge(challengeId);
        return ResponseEntity.ok(achievements);
    }
    
    /**
     * Verifica si un usuario tiene un logro específico
     */
    @GetMapping("/user/{userId}/has/{achievementType}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Boolean> hasUserAchievement(
            @PathVariable Long userId,
            @PathVariable AchievementType achievementType) {
        log.info("Verificando si usuario {} tiene logro: {}", userId, achievementType);
        boolean hasAchievement = achievementService.hasUserAchievement(userId, achievementType);
        return ResponseEntity.ok(hasAchievement);
    }
    
    /**
     * Endpoint administrativo para desbloquear logros manualmente
     */
    @PostMapping("/admin/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unlockAchievementManually(
            @RequestParam Long userId,
            @RequestParam AchievementType achievementType,
            @RequestParam(required = false) Long challengeId,
            @RequestParam(required = false) String contextInfo) {
        
        log.info("Administrador desbloqueando logro manualmente: {} para usuario {}", 
            achievementType, userId);
        
        achievementEvaluationService.unlockAchievementManually(
            userId, achievementType, challengeId, contextInfo);
        
        return ResponseEntity.ok("Logro desbloqueado exitosamente");
    }
} 