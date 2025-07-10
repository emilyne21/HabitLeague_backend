package com.example.habitleague.challenge.schedule;

import com.example.habitleague.challenge.service.ChallengeLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyLifecycleScheduler {
    
    private final ChallengeLifecycleService lifecycleService;
    
    /**
     * Ejecuta la verificación diaria de evidencias y gestión del pricepool
     * Se ejecuta todos los días a las 00:05 AM
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Mexico_City")
    public void runDailyLifecycleCheck() {
        try {
            log.info("=== INICIANDO CICLO DIARIO DE CHALLENGES ===");
            lifecycleService.performDailyLifecycleCheck();
            log.info("=== CICLO DIARIO COMPLETADO EXITOSAMENTE ===");
        } catch (Exception e) {
            log.error("=== ERROR EN CICLO DIARIO ===: {}", e.getMessage(), e);
            // En un sistema real, aquí se enviaría una alerta o notificación
        }
    }
    
    /**
     * Método de prueba para ejecutar manualmente el ciclo diario
     * (solo para desarrollo y testing)
     */
    public void runManualCheck() {
        log.info("Ejecutando verificación manual del ciclo diario");
        runDailyLifecycleCheck();
    }
} 