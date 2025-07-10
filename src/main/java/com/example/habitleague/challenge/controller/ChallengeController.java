package com.example.habitleague.challenge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.habitleague.challenge.dto.*;
import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeCategory;
import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.challenge.service.ChallengeService;
import com.example.habitleague.challenge.service.ChallengeLifecycleService;
import com.example.habitleague.challenge.schedule.DailyLifecycleScheduler;
import com.example.habitleague.location.dto.LocationRegistrationRequest;
import com.example.habitleague.location.dto.LocationRegistrationResponse;
import com.example.habitleague.location.service.LocationRegistrationService;
import com.example.habitleague.payment.dto.PaymentRequest;
import com.example.habitleague.payment.dto.PaymentResponse;
import com.example.habitleague.payment.service.PaymentService;
import com.example.habitleague.shared.exception.ChallengeException;
import com.example.habitleague.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final PaymentService paymentService;
    private final LocationRegistrationService locationRegistrationService;
    private final ChallengeLifecycleService lifecycleService;
    private final DailyLifecycleScheduler dailyScheduler;

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> createChallenge(
            @RequestBody @Valid CreateChallengeCompleteRequest request,
            @AuthenticationPrincipal User user) {
        
        // Paso 1: Procesar el pago del creador
        PaymentResponse paymentResponse;
        try {
            // El creador paga por su propio challenge
            request.getPayment().setAmount(request.getEntryFee());
            Challenge tempChallenge = challengeService.createChallengeWithoutMember(request, user);
            
            // Convertir a PaymentRequest con challengeId
            PaymentRequest paymentRequest = request.getPayment().toPaymentRequest(tempChallenge.getId());
            
            paymentResponse = paymentService.processPayment(paymentRequest, user);
            
            if (!paymentResponse.getStatus().name().equals("SUCCEEDED")) {
                // Si el pago falla, eliminar el challenge creado
                challengeService.deleteChallenge(tempChallenge.getId());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "El pago no pudo ser procesado",
                    "paymentStatus", paymentResponse.getStatus()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error procesando el pago: " + e.getMessage()
            ));
        }

        // Paso 2: Obtener el challenge y unir al creador
        Challenge challenge;
        ChallengeMember creatorMember;
        try {
            challenge = challengeService.getChallengeById(paymentResponse.getChallengeId());
            // Crear miembro con pago completado pero ubicación pendiente
            creatorMember = challengeService.createChallengeMemberPartial(
                challenge.getId(), user, true, false);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error uniendo el creador al challenge: " + e.getMessage(),
                "paymentCompleted", true
            ));
        }

        // Paso 3: Registrar la ubicación del creador
        LocationRegistrationResponse locationResponse;
        try {
            // Convertir a LocationRegistrationRequest con challengeId
            LocationRegistrationRequest locationRequest = request.getLocation().toLocationRegistrationRequest(challenge.getId());
            
            locationResponse = locationRegistrationService.registerLocation(locationRequest, creatorMember);
            
            // Actualizar el estado de ubicación del miembro
            creatorMember = challengeService.updateChallengeMemberLocation(creatorMember, true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error registrando ubicación: " + e.getMessage(),
                "paymentCompleted", true,
                "challengeCreated", true
            ));
        }

        // Respuesta exitosa
        Map<String, Object> response = Map.of(
            "message", "Challenge creado exitosamente",
            "challenge", ChallengeResponse.fromChallenge(challenge),
            "payment", Map.of(
                "paymentId", paymentResponse.getPaymentId(),
                "amount", paymentResponse.getAmount(),
                "currency", paymentResponse.getCurrency(),
                "status", paymentResponse.getStatus()
            ),
            "location", Map.of(
                "registrationId", locationResponse.getRegistrationId(),
                "address", locationResponse.getAddress(),
                "locationName", locationResponse.getLocationName(),
                "registeredAt", locationResponse.getRegisteredAt()
            ),
            "membership", Map.of(
                "memberId", creatorMember.getId(),
                "joinedAt", creatorMember.getJoinedAt(),
                "paymentCompleted", creatorMember.getPaymentCompleted(),
                "locationRegistered", creatorMember.getLocationRegistered()
            )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges() {
        List<Challenge> challenges = challengeService.getAllChallenges();
        List<ChallengeResponse> response = challenges.stream()
                .map(ChallengeResponse::fromChallenge)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/participants")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChallengeParticipantResponse>> getParticipants(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        List<ChallengeMember> participants = challengeService.getParticipants(id, user);
        
        List<ChallengeParticipantResponse> response = participants.stream()
            .map(member -> ChallengeParticipantResponse.builder()
                .id(member.getUser().getId())
                .name(member.getUser().getFirstName() + " " + member.getUser().getLastName())
                .email(member.getUser().getEmail())
                .joinedAt(member.getJoinedAt())
                .build())
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }

    // Nuevos endpoints
    @GetMapping("/featured")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChallengeSummaryResponse>> getFeaturedChallenges() {
        List<Challenge> challenges = challengeService.getFeaturedChallenges();
        List<ChallengeSummaryResponse> response = challenges.stream()
                .map(ChallengeSummaryResponse::fromChallenge)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChallengeSummaryResponse>> getPopularChallenges(
            @RequestParam(defaultValue = "10") int limit) {
        List<Challenge> challenges = challengeService.getPopularChallenges(limit);
        List<ChallengeSummaryResponse> response = challenges.stream()
                .map(ChallengeSummaryResponse::fromChallenge)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChallengeSummaryResponse>> getChallengesByCategory(
            @PathVariable ChallengeCategory category) {
        List<Challenge> challenges = challengeService.getChallengesByCategory(category);
        List<ChallengeSummaryResponse> response = challenges.stream()
                .map(ChallengeSummaryResponse::fromChallenge)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ChallengeResponse> getChallengeById(@PathVariable Long id) {
        Challenge challenge = challengeService.getChallengeById(id);
        return ResponseEntity.ok(ChallengeResponse.fromChallenge(challenge));
    }

    @GetMapping("/my-challenges")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserChallengeResponse>> getUserChallenges(
            @AuthenticationPrincipal User user) {
        List<ChallengeMember> userChallenges = challengeService.getUserChallenges(user);
        List<UserChallengeResponse> response = userChallenges.stream()
                .map(UserChallengeResponse::fromChallengeMember)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/discover")
    @Transactional(readOnly = true)
    public ResponseEntity<DiscoverResponse> getDiscoverData() {
        // Obtener challenges destacados
        List<Challenge> featured = challengeService.getFeaturedChallenges();
        
        // Obtener challenges populares
        List<Challenge> popular = challengeService.getPopularChallenges(10);
        
        // Obtener challenges por categoría
        Map<ChallengeCategory, List<ChallengeSummaryResponse>> byCategory = 
            Map.of(
                ChallengeCategory.MINDFULNESS, 
                challengeService.getChallengesByCategory(ChallengeCategory.MINDFULNESS)
                    .stream().map(ChallengeSummaryResponse::fromChallenge).collect(Collectors.toList()),
                ChallengeCategory.FITNESS,
                challengeService.getChallengesByCategory(ChallengeCategory.FITNESS)
                    .stream().map(ChallengeSummaryResponse::fromChallenge).collect(Collectors.toList()),
                ChallengeCategory.PRODUCTIVITY,
                challengeService.getChallengesByCategory(ChallengeCategory.PRODUCTIVITY)
                    .stream().map(ChallengeSummaryResponse::fromChallenge).collect(Collectors.toList())
            );

        DiscoverResponse response = DiscoverResponse.builder()
                .featured(featured.stream().map(ChallengeSummaryResponse::fromChallenge).collect(Collectors.toList()))
                .popular(popular.stream().map(ChallengeSummaryResponse::fromChallenge).collect(Collectors.toList()))
                .byCategory(byCategory)
                .build();

        return ResponseEntity.ok(response);
    }

    // Endpoint para unirse a un challenge (solo para usuarios que no son creadores)
    @PostMapping("/{id}/join")
    public ResponseEntity<Map<String, Object>> joinChallenge(
            @PathVariable Long id,
            @RequestBody @Valid JoinChallengeCompleteRequest request,
            @AuthenticationPrincipal User user) {

        try {
            // Paso 1: Procesar el pago
            request.getPayment().setChallengeId(id); // Asegurar que el challengeId coincida
            PaymentResponse paymentResponse = paymentService.processPayment(request.getPayment(), user);
            
            if (!paymentResponse.getStatus().name().equals("SUCCEEDED")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "El pago no pudo ser procesado",
                    "paymentStatus", paymentResponse.getStatus(),
                    "paymentId", paymentResponse.getPaymentId()
                ));
            }

            // Paso 2: Crear el ChallengeMember temporal para la verificación de ubicación
            // Crear miembro con pago completado pero ubicación pendiente
            ChallengeMember tempMember = challengeService.createChallengeMemberPartial(id, user, true, false);

            // Paso 3: Registrar la ubicación
            request.getLocation().setChallengeId(id); // Asegurar que el challengeId coincida
            LocationRegistrationResponse locationResponse = locationRegistrationService.registerLocation(request.getLocation(), tempMember);
            
            // Actualizar el estado de ubicación del miembro
            tempMember = challengeService.updateChallengeMemberLocation(tempMember, true);

            // Si llegamos aquí, tanto el pago como el registro fueron exitosos
            Map<String, Object> response = Map.of(
                "message", "Te uniste al reto exitosamente",
                "challengeName", paymentResponse.getChallengeName(),
                "payment", Map.of(
                    "paymentId", paymentResponse.getPaymentId(),
                    "amount", paymentResponse.getAmount(),
                    "currency", paymentResponse.getCurrency(),
                    "status", paymentResponse.getStatus()
                ),
                "location", Map.of(
                    "registrationId", locationResponse.getRegistrationId(),
                    "address", locationResponse.getAddress(),
                    "locationName", locationResponse.getLocationName(),
                    "registeredAt", locationResponse.getRegisteredAt(),
                    "status", "VERIFIED"
                ),
                "membership", Map.of(
                    "memberId", tempMember.getId(),
                    "joinedAt", tempMember.getJoinedAt(),
                    "paymentCompleted", tempMember.getPaymentCompleted(),
                    "locationRegistered", tempMember.getLocationRegistered()
                )
            );

            return ResponseEntity.ok(response);
            
        } catch (ChallengeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "type", "CHALLENGE_ERROR"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error procesando la solicitud: " + e.getMessage(),
                "type", "GENERAL_ERROR"
            ));
        }
    }

    // Endpoint para verificar los requisitos antes de unirse a un challenge
    @GetMapping("/{id}/requirements-status")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getRequirementsStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        Challenge challenge = challengeService.getChallengeById(id);
        
        // Verificar si el usuario ya está en el challenge
        boolean alreadyJoined = challengeService.getAllChallenges().stream()
                .anyMatch(c -> c.getId().equals(id) && c.getMembers().stream()
                        .anyMatch(member -> member.getUser().getId().equals(user.getId())));

        // Verificar estado del pago
        boolean paymentCompleted = paymentService.hasUserPaidForChallenge(user, challenge);

        // Verificar estado de la ubicación (esto requerirá algo de lógica adicional)
        boolean locationVerified = false; // Por ahora false, se implementaría la lógica real

        Map<String, Object> response = Map.of(
            "challengeId", id,
            "challengeName", challenge.getName(),
            "entryFee", challenge.getEntryFee(),
            "alreadyJoined", alreadyJoined,
            "requirements", Map.of(
                "paymentCompleted", paymentCompleted,
                "locationVerified", locationVerified
            ),
            "canJoin", !alreadyJoined && !paymentCompleted && !locationVerified
        );

        return ResponseEntity.ok(response);
    }

    // ===== NUEVOS ENDPOINTS PARA PRICEPOOL =====
    
    /**
     * Obtiene información del pricepool de un challenge
     */
    @GetMapping("/{id}/pricepool")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPricepoolInfo(@PathVariable Long id) {
        try {
            ChallengeLifecycleService.PricepoolStats stats = lifecycleService.getPricepoolStats(id);
            
            Map<String, Object> response = Map.of(
                "challengeId", stats.challengeId,
                "totalPrizepool", stats.totalPrizepool,
                "activeParticipants", stats.activeParticipants,
                "prizePerWinner", stats.prizePerWinner,
                "prizesDistributed", stats.prizesDistributed,
                "status", stats.prizesDistributed ? "COMPLETED" : "ACTIVE"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error obteniendo información del pricepool: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para testing: ejecutar verificación manual del ciclo diario
     * (Solo para desarrollo - en producción se removería o se protegería con roles de admin)
     */
    @PostMapping("/admin/run-daily-check")
    public ResponseEntity<Map<String, Object>> runManualDailyCheck() {
        try {
            dailyScheduler.runManualCheck();
            return ResponseEntity.ok(Map.of(
                "message", "Verificación diaria ejecutada manualmente",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error ejecutando verificación: " + e.getMessage()
            ));
        }
    }
} 