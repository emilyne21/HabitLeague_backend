package com.example.habitleague.evidence.service;

import com.example.habitleague.challenge.model.ChallengeCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIValidationService {

    /**
     * Simula la validación de una imagen usando IA
     * Retorna true el 50% de las veces para simular el comportamiento
     */
    public boolean validateEvidenceImage(String imageUrl, ChallengeCategory category) {
        log.info("Validando imagen con IA simulada: {} para categoría: {}", imageUrl, category);
        
        try {
            // Simular tiempo de procesamiento de IA
            Thread.sleep(100);
            
            // Generar resultado aleatorio (50% de probabilidad de éxito)
            boolean isValid = Math.random() < 0.5;
            
            if (isValid) {
                log.info("✅ IA VÁLIDA: Imagen {} aprobada para categoría {}", imageUrl, category);
            } else {
                log.warn("❌ IA INVÁLIDA: Imagen {} rechazada para categoría {}", imageUrl, category);
            }
            
            return isValid;
            
        } catch (InterruptedException e) {
            log.error("Error simulando validación de IA: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Simula análisis detallado de la imagen para propósitos de logging
     */
    public AIValidationResult validateWithDetails(String imageUrl, ChallengeCategory category) {
        boolean isValid = validateEvidenceImage(imageUrl, category);
        
        return AIValidationResult.builder()
            .imageUrl(imageUrl)
            .category(category)
            .isValid(isValid)
            .confidence(Math.random()) // Simular confianza aleatoria 0-1
            .reason(generateSimulatedReason(isValid, category))
            .build();
    }

    private String generateSimulatedReason(boolean isValid, ChallengeCategory category) {
        if (isValid) {
            return switch (category) {
                case FITNESS -> "Imagen muestra actividad física válida detectada";
                case MINDFULNESS -> "Postura de meditación o ambiente mindfulness identificado";
                case PRODUCTIVITY -> "Espacio de trabajo o actividad productiva reconocida";
                case HEALTH -> "Actividad relacionada con salud detectada";
                case CODING -> "Entorno de programación o código identificado";
                case READING -> "Libro o material de lectura detectado";
                case WRITING -> "Actividad de escritura identificada";
                case CREATIVITY -> "Actividad creativa o artística reconocida";
                default -> "Actividad válida para la categoría detectada";
            };
        } else {
            return switch (category) {
                case FITNESS -> "No se detecta actividad física válida";
                case MINDFULNESS -> "Imagen no corresponde a actividad mindfulness";
                case PRODUCTIVITY -> "Actividad no relacionada con productividad";
                case HEALTH -> "No se identifica actividad saludable";
                case CODING -> "No se detecta actividad de programación";
                case READING -> "No se identifica material de lectura";
                case WRITING -> "Actividad de escritura no detectada";
                case CREATIVITY -> "No se reconoce actividad creativa";
                default -> "Imagen no corresponde a la categoría del challenge";
            };
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class AIValidationResult {
        private String imageUrl;
        private ChallengeCategory category;
        private boolean isValid;
        private double confidence;
        private String reason;
    }
} 