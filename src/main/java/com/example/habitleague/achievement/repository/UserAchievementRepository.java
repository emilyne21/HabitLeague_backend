package com.example.habitleague.achievement.repository;

import com.example.habitleague.achievement.model.AchievementType;
import com.example.habitleague.achievement.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad UserAchievement
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    /**
     * Busca todos los logros desbloqueados por un usuario
     */
    @Query("SELECT ua FROM UserAchievement ua " +
           "JOIN FETCH ua.achievement " +
           "WHERE ua.user.id = :userId " +
           "ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findByUserIdWithAchievement(@Param("userId") Long userId);
    
    /**
     * Verifica si un usuario tiene un logro específico
     */
    @Query("SELECT ua FROM UserAchievement ua " +
           "JOIN ua.achievement a " +
           "WHERE ua.user.id = :userId AND a.type = :achievementType")
    Optional<UserAchievement> findByUserIdAndAchievementType(
        @Param("userId") Long userId, 
        @Param("achievementType") AchievementType achievementType);
    
    /**
     * Cuenta el número de logros desbloqueados por un usuario
     */
    Long countByUserId(Long userId);
    
    /**
     * Verifica si un usuario ya tiene un logro específico
     */
    @Query("SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END " +
           "FROM UserAchievement ua " +
           "JOIN ua.achievement a " +
           "WHERE ua.user.id = :userId AND a.type = :achievementType")
    boolean existsByUserIdAndAchievementType(
        @Param("userId") Long userId, 
        @Param("achievementType") AchievementType achievementType);
    
    /**
     * Obtiene logros recientes de un usuario (últimos 10)
     */
    @Query("SELECT ua FROM UserAchievement ua " +
           "JOIN FETCH ua.achievement " +
           "WHERE ua.user.id = :userId " +
           "ORDER BY ua.unlockedAt DESC " +
           "LIMIT 10")
    List<UserAchievement> findRecentByUserId(@Param("userId") Long userId);
    
    /**
     * Busca logros por challenge ID
     */
    @Query("SELECT ua FROM UserAchievement ua " +
           "JOIN FETCH ua.achievement " +
           "WHERE ua.challengeId = :challengeId")
    List<UserAchievement> findByChallengeId(@Param("challengeId") Long challengeId);
} 