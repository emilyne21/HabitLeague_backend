package com.example.habitleague.evidence.service;

import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.challenge.repository.ChallengeMemberRepository;
import com.example.habitleague.challenge.repository.ChallengeRepository;
import com.example.habitleague.evidence.dto.EvidenceResponse;
import com.example.habitleague.evidence.dto.SubmitEvidenceRequest;
import com.example.habitleague.evidence.model.Evidence;
import com.example.habitleague.evidence.repository.EvidenceRepository;
import com.example.habitleague.location.model.EvidenceLocationVerification;
import com.example.habitleague.location.repository.EvidenceLocationVerificationRepository;
import com.example.habitleague.location.service.EvidenceLocationVerificationService;
import com.example.habitleague.shared.exception.ChallengeException;
import com.example.habitleague.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeRepository challengeRepository;
    private final EvidenceLocationVerificationRepository verificationRepository;
    private final AIValidationService aiValidationService;
    private final EvidenceLocationVerificationService locationVerificationService;

    /**
     * Procesa el envío de una evidencia diaria
     */
    @Transactional
    public EvidenceResponse submitEvidence(SubmitEvidenceRequest request, User user) {
        log.info("Procesando evidencia para usuario {} en challenge {}", user.getEmail(), request.getChallengeId());

        // 1. Verificar que el challenge existe
        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ChallengeException("Challenge no encontrado"));

        // 2. Verificar que el usuario es miembro del challenge
        ChallengeMember challengeMember = challengeMemberRepository
                .findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new ChallengeException("No eres miembro de este challenge"));

        // 3. Verificar que el miembro tiene pago y ubicación completados
        if (!challengeMember.getPaymentCompleted()) {
            throw new ChallengeException("Debes completar el pago antes de enviar evidencias");
        }
        if (!challengeMember.getLocationRegistered()) {
            throw new ChallengeException("Debes registrar tu ubicación antes de enviar evidencias");
        }

        // 4. Verificar que no ha enviado evidencia hoy
        if (hasSubmittedEvidenceToday(challengeMember)) {
            throw new ChallengeException("Ya enviaste tu evidencia diaria para hoy");
        }

        // 5. Crear evidencia inicial (sin validaciones aún)
        Evidence evidence = Evidence.builder()
                .challengeMember(challengeMember)
                .imageUrl(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .aiValidated(false)
                .locationValid(false)
                .build();

        evidence = evidenceRepository.save(evidence);
        log.info("Evidencia inicial creada con ID: {}", evidence.getId());

        // 6. Validar con IA (simulada)
        boolean aiResult = aiValidationService.validateEvidenceImage(
            request.getImageUrl(), 
            challenge.getCategory()
        );
        evidence.setAiValidated(aiResult);

        // 7. Verificar ubicación
        EvidenceLocationVerification locationVerification = locationVerificationService
                .verifyLocationForEvidence(evidence, request.getLatitude(), request.getLongitude());
        
        verificationRepository.save(locationVerification);
        evidence.setLocationValid(locationVerification.getIsWithinTolerance());

        // 8. Guardar evidencia actualizada
        evidence = evidenceRepository.save(evidence);

        // 9. Log del resultado
        logEvidenceResult(evidence, locationVerification);

        return EvidenceResponse.fromEvidence(evidence);
    }

    /**
     * Obtiene todas las evidencias de un usuario
     */
    @Transactional(readOnly = true)
    public List<EvidenceResponse> getUserEvidences(User user) {
        List<Evidence> evidences = evidenceRepository.findByChallengeMember_User_Id(user.getId());
        return evidences.stream()
                .map(EvidenceResponse::fromEvidence)
                .toList();
    }

    /**
     * Obtiene evidencias por challenge
     */
    @Transactional(readOnly = true)
    public List<EvidenceResponse> getEvidencesByChallenge(Long challengeId, User user) {
        // Verificar que el usuario tiene acceso al challenge
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException("Challenge no encontrado"));

        ChallengeMember challengeMember = challengeMemberRepository
                .findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new ChallengeException("No tienes acceso a este challenge"));

        // Retornar solo las evidencias del usuario para este challenge
        List<Evidence> evidences = evidenceRepository.findByChallengeMember_User_Id(user.getId());
        return evidences.stream()
                .filter(e -> e.getChallengeMember().getChallenge().getId().equals(challengeId))
                .map(EvidenceResponse::fromEvidence)
                .toList();
    }

    /**
     * Obtiene el estado de evidencia para hoy
     */
    @Transactional(readOnly = true)
    public boolean hasSubmittedEvidenceToday(User user, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException("Challenge no encontrado"));

        ChallengeMember challengeMember = challengeMemberRepository
                .findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new ChallengeException("No eres miembro de este challenge"));

        return hasSubmittedEvidenceToday(challengeMember);
    }

    /**
     * Verifica si un miembro ya envió evidencia hoy
     */
    private boolean hasSubmittedEvidenceToday(ChallengeMember challengeMember) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return evidenceRepository.existsByChallengeMemberAndSubmittedAtBetween(
                challengeMember, startOfDay, endOfDay);
    }

    /**
     * Obtiene estadísticas de validación para un usuario
     */
    @Transactional(readOnly = true)
    public EvidenceStats getUserEvidenceStats(User user) {
        List<Evidence> userEvidences = evidenceRepository.findByChallengeMember_User_Id(user.getId());
        
        long totalEvidences = userEvidences.size();
        long aiValidated = userEvidences.stream().mapToLong(e -> e.getAiValidated() ? 1 : 0).sum();
        long locationValid = userEvidences.stream().mapToLong(e -> e.getLocationValid() ? 1 : 0).sum();
        long bothValid = userEvidences.stream().mapToLong(e -> e.getAiValidated() && e.getLocationValid() ? 1 : 0).sum();

        double aiSuccessRate = totalEvidences > 0 ? (aiValidated * 100.0 / totalEvidences) : 0.0;
        double locationSuccessRate = totalEvidences > 0 ? (locationValid * 100.0 / totalEvidences) : 0.0;
        double overallSuccessRate = totalEvidences > 0 ? (bothValid * 100.0 / totalEvidences) : 0.0;

        return EvidenceStats.builder()
            .totalEvidences(totalEvidences)
            .aiValidated(aiValidated)
            .locationValid(locationValid)
            .bothValid(bothValid)
            .aiSuccessRate(aiSuccessRate)
            .locationSuccessRate(locationSuccessRate)
            .overallSuccessRate(overallSuccessRate)
            .build();
    }

    private void logEvidenceResult(Evidence evidence, EvidenceLocationVerification verification) {
        String aiStatus = evidence.getAiValidated() ? "✅ VÁLIDA" : "❌ INVÁLIDA";
        String locationStatus = evidence.getLocationValid() ? "✅ VÁLIDA" : "❌ INVÁLIDA";
        
        log.info("📊 RESULTADO EVIDENCIA ID {}: IA: {} | Ubicación: {} | Distancia: {:.1f}m", 
                evidence.getId(), aiStatus, locationStatus, verification.getDistanceFromRegistered());
        
        if (!evidence.getAiValidated() || !evidence.getLocationValid()) {
            log.warn("⚠️ EVIDENCIA RECHAZADA - Usuario: {} | Challenge: {} | Razón: {}", 
                    evidence.getChallengeMember().getUser().getEmail(),
                    evidence.getChallengeMember().getChallenge().getName(),
                    !evidence.getAiValidated() ? "IA rechazada" : "Ubicación inválida");
        } else {
            log.info("🎉 EVIDENCIA APROBADA - Usuario: {} | Challenge: {}", 
                    evidence.getChallengeMember().getUser().getEmail(),
                    evidence.getChallengeMember().getChallenge().getName());
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class EvidenceStats {
        private long totalEvidences;
        private long aiValidated;
        private long locationValid;
        private long bothValid;
        private double aiSuccessRate;
        private double locationSuccessRate;
        private double overallSuccessRate;
    }
} 