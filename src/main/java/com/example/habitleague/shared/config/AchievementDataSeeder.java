package com.example.habitleague.shared.config;

import com.example.habitleague.achievement.model.Achievement;
import com.example.habitleague.achievement.model.AchievementType;
import com.example.habitleague.achievement.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Componente para poblar la base de datos con logros iniciales
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AchievementDataSeeder implements CommandLineRunner {
    
    private final AchievementRepository achievementRepository;
    
    @Override
    public void run(String... args) throws Exception {
        seedAchievements();
    }
    
    /**
     * Pobla la base de datos con los logros iniciales si no existen
     */
    private void seedAchievements() {
        if (achievementRepository.count() == 0) {
            log.info("Poblando base de datos con logros iniciales...");
            
            List<Achievement> achievements = Arrays.asList(
                Achievement.builder()
                    .type(AchievementType.FIRST_CHALLENGE_COMPLETED)
                    .name("Primer reto completado")
                    .description("Por completar un reto de cualquier duración.")
                    .iconUrl("🎯")
                    .isActive(true)
                    .build(),
                
                Achievement.builder()
                    .type(AchievementType.SEVEN_DAY_STREAK)
                    .name("Racha de 7 días")
                    .description("Por cumplir 7 días seguidos sin fallar.")
                    .iconUrl("🔥")
                    .isActive(true)
                    .build(),
                
                Achievement.builder()
                    .type(AchievementType.PERFECT_CHALLENGE)
                    .name("Sin excusas")
                    .description("Por no fallar ni un solo día en un reto.")
                    .iconUrl("💎")
                    .isActive(true)
                    .build(),
                
                Achievement.builder()
                    .type(AchievementType.FIRST_PENALTY_PAYMENT)
                    .name("Primer pago de penalización")
                    .description("Por haber tenido que pagar por incumplir (para incentivar la reflexión).")
                    .iconUrl("💰")
                    .isActive(true)
                    .build()
            );
            
            achievementRepository.saveAll(achievements);
            log.info("✅ Se han creado {} logros iniciales", achievements.size());
            
            // Log de confirmación
            achievements.forEach(achievement -> 
                log.info("Logro creado: {} - {}", achievement.getName(), achievement.getDescription())
            );
        } else {
            log.info("Los logros ya existen en la base de datos. Total: {}", achievementRepository.count());
        }
    }
} 