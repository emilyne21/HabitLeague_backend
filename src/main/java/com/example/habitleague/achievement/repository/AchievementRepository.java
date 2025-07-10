package com.example.habitleague.achievement.repository;

import com.example.habitleague.achievement.model.Achievement;
import com.example.habitleague.achievement.model.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Achievement
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    /**
     * Busca un logro por su tipo
     */
    Optional<Achievement> findByType(AchievementType type);
    
    /**
     * Busca todos los logros activos
     */
    List<Achievement> findByIsActiveTrue();
    
    /**
     * Busca logros por tipo y estado activo
     */
    Optional<Achievement> findByTypeAndIsActiveTrue(AchievementType type);
    
    /**
     * Cuenta el número total de logros activos
     */
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.isActive = true")
    Long countActiveAchievements();
} 