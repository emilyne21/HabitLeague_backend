package com.example.habitleague.evidence.controller;

import com.example.habitleague.evidence.dto.EvidenceResponse;
import com.example.habitleague.evidence.dto.SubmitEvidenceRequest;
import com.example.habitleague.evidence.service.EvidenceService;
import com.example.habitleague.evidence.service.EvidenceService.EvidenceStats;
import com.example.habitleague.shared.exception.ChallengeException;
import com.example.habitleague.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evidences")
@RequiredArgsConstructor
@Slf4j
public class EvidenceController {

    private final EvidenceService evidenceService;

    /**
     * Endpoint para enviar evidencia diaria
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> submitEvidence(
            @RequestBody @Valid SubmitEvidenceRequest request,
            @AuthenticationPrincipal User user) {
        
        try {
            log.info("Solicitud de evidencia recibida de usuario: {} para challenge: {}", 
                    user.getEmail(), request.getChallengeId());

            EvidenceResponse evidence = evidenceService.submitEvidence(request, user);
            
            // Determinar el estado general de la evidencia
            boolean isFullyValid = evidence.getAiValidated() && evidence.getLocationValid();
            String status = isFullyValid ? "APPROVED" : "REJECTED";
            String message = isFullyValid ? 
                "¡Evidencia enviada y aprobada exitosamente!" : 
                "Evidencia enviada pero no cumple todos los criterios de validación";

            Map<String, Object> response = Map.of(
                "success", true,
                "message", message,
                "status", status,
                "evidence", evidence,
                "validation", Map.of(
                    "aiValidated", evidence.getAiValidated(),
                    "locationValid", evidence.getLocationValid(),
                    "fullyValid", isFullyValid
                ),
                "nextSubmission", "Podrás enviar tu próxima evidencia mañana"
            );

            if (isFullyValid) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(response); // 200 pero con estado de rechazo
            }

        } catch (ChallengeException e) {
            log.warn("Error de challenge al enviar evidencia: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "type", "CHALLENGE_ERROR"
            ));
        } catch (Exception e) {
            log.error("Error inesperado al procesar evidencia: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error interno del servidor procesando la evidencia",
                "type", "INTERNAL_ERROR"
            ));
        }
    }

    /**
     * Obtener todas las evidencias del usuario
     */
    @GetMapping("/my-evidences")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EvidenceResponse>> getMyEvidences(
            @AuthenticationPrincipal User user) {
        
        try {
            List<EvidenceResponse> evidences = evidenceService.getUserEvidences(user);
            return ResponseEntity.ok(evidences);
        } catch (Exception e) {
            log.error("Error obteniendo evidencias del usuario {}: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener evidencias por challenge específico
     */
    @GetMapping("/challenge/{challengeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EvidenceResponse>> getEvidencesByChallenge(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal User user) {
        
        try {
            List<EvidenceResponse> evidences = evidenceService.getEvidencesByChallenge(challengeId, user);
            return ResponseEntity.ok(evidences);
        } catch (ChallengeException e) {
            log.warn("Error de acceso al challenge {}: {}", challengeId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error obteniendo evidencias del challenge {}: {}", challengeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verificar si el usuario ya envió evidencia hoy para un challenge
     */
    @GetMapping("/challenge/{challengeId}/daily-status")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getDailySubmissionStatus(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal User user) {
        
        try {
            boolean hasSubmitted = evidenceService.hasSubmittedEvidenceToday(user, challengeId);
            
            Map<String, Object> response = Map.of(
                "challengeId", challengeId,
                "hasSubmittedToday", hasSubmitted,
                "canSubmit", !hasSubmitted,
                "message", hasSubmitted ? 
                    "Ya enviaste tu evidencia diaria para hoy" : 
                    "Puedes enviar tu evidencia diaria",
                "submissionWindow", "00:00 - 23:59"
            );

            return ResponseEntity.ok(response);

        } catch (ChallengeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "type", "CHALLENGE_ERROR"
            ));
        } catch (Exception e) {
            log.error("Error verificando estado diario: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error verificando estado de evidencia diaria",
                "type", "INTERNAL_ERROR"
            ));
        }
    }

    /**
     * Obtener estadísticas de evidencias del usuario
     */
    @GetMapping("/my-stats")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMyEvidenceStats(
            @AuthenticationPrincipal User user) {
        
        try {
            EvidenceStats stats = evidenceService.getUserEvidenceStats(user);
            
            Map<String, Object> response = Map.of(
                "userId", user.getId(),
                "userName", user.getFirstName() + " " + user.getLastName(),
                "statistics", Map.of(
                    "totalEvidences", stats.getTotalEvidences(),
                    "aiValidated", stats.getAiValidated(),
                    "locationValid", stats.getLocationValid(),
                    "bothValid", stats.getBothValid(),
                    "successRates", Map.of(
                        "ai", Math.round(stats.getAiSuccessRate() * 100.0) / 100.0,
                        "location", Math.round(stats.getLocationSuccessRate() * 100.0) / 100.0,
                        "overall", Math.round(stats.getOverallSuccessRate() * 100.0) / 100.0
                    )
                ),
                "interpretation", generateStatsInterpretation(stats)
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de usuario {}: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error obteniendo estadísticas de evidencias",
                "type", "INTERNAL_ERROR"
            ));
        }
    }

    /**
     * Endpoint de salud para verificar que el servicio de evidencias funciona
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "service", "EvidenceService",
            "status", "UP",
            "timestamp", System.currentTimeMillis(),
            "features", List.of(
                "AI Validation (Simulated 50% success)",
                "Location Verification",
                "Daily Evidence Submission",
                "Evidence Statistics"
            )
        ));
    }

    /**
     * Generar interpretación de estadísticas para el usuario
     */
    private Map<String, String> generateStatsInterpretation(EvidenceStats stats) {
        String aiMessage;
        String locationMessage;
        String overallMessage;

        // Interpretar tasa de éxito de IA
        if (stats.getAiSuccessRate() >= 70) {
            aiMessage = "¡Excelente! Tus fotos cumplen muy bien los criterios del challenge";
        } else if (stats.getAiSuccessRate() >= 50) {
            aiMessage = "Bien. Intenta tomar fotos más claras que muestren la actividad";
        } else {
            aiMessage = "Mejora necesaria. Asegúrate de que tus fotos muestren claramente la actividad del challenge";
        }

        // Interpretar tasa de éxito de ubicación
        if (stats.getLocationSuccessRate() >= 90) {
            locationMessage = "Perfecto. Siempre envías evidencias desde la ubicación correcta";
        } else if (stats.getLocationSuccessRate() >= 70) {
            locationMessage = "Muy bien. Procura estar siempre en tu ubicación registrada";
        } else {
            locationMessage = "Atención. Debes enviar evidencias desde tu ubicación registrada";
        }

        // Interpretar tasa general
        if (stats.getOverallSuccessRate() >= 70) {
            overallMessage = "¡Excelente participación! Sigues muy bien las reglas del challenge";
        } else if (stats.getOverallSuccessRate() >= 50) {
            overallMessage = "Buena participación. Hay margen de mejora en la calidad de evidencias";
        } else {
            overallMessage = "Necesitas mejorar la calidad de tus evidencias para tener éxito en el challenge";
        }

        return Map.of(
            "ai", aiMessage,
            "location", locationMessage,
            "overall", overallMessage
        );
    }
} 