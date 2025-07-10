package com.example.habitleague.location.service;

import com.example.habitleague.evidence.model.Evidence;
import com.example.habitleague.location.model.EvidenceLocationVerification;
import com.example.habitleague.location.model.LocationVerificationResult;
import com.example.habitleague.location.model.RegisteredLocation;
import com.example.habitleague.location.repository.RegisteredLocationRepository;
import com.example.habitleague.shared.exception.ChallengeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceLocationVerificationService {

    private final RegisteredLocationRepository registeredLocationRepository;

    /**
     * Verifica si la ubicación actual del usuario está dentro del rango permitido
     * comparando con la ubicación registrada al unirse al challenge
     */
    public EvidenceLocationVerification verifyLocationForEvidence(
            Evidence evidence,
            Double currentLatitude,
            Double currentLongitude) {

        // Obtener la ubicación registrada del usuario para este challenge
        RegisteredLocation registeredLocation = registeredLocationRepository
                .findByUserIdAndChallengeId(evidence.getChallengeMember().getUser().getId(), evidence.getChallengeMember().getChallenge().getId())
                .orElseThrow(() -> new ChallengeException("No hay ubicación registrada para este usuario en el challenge"));

        // Calcular distancia entre ubicación actual y registrada
        double distance = calculateDistance(
                currentLatitude, currentLongitude,
                registeredLocation.getLatitude(), registeredLocation.getLongitude()
        );

        // Verificar si está dentro del radio de tolerancia
        boolean isWithinTolerance = distance <= registeredLocation.getToleranceRadius();

        // Determinar el resultado de la verificación
        LocationVerificationResult result;
        if (isWithinTolerance) {
            result = LocationVerificationResult.VERIFIED;
        } else if (distance > registeredLocation.getToleranceRadius() * 3) {
            // Si está muy lejos, podría ser sospechoso
            result = LocationVerificationResult.SUSPICIOUS;
        } else {
            result = LocationVerificationResult.OUT_OF_RANGE;
        }

        // Crear el registro de verificación
        return EvidenceLocationVerification.builder()
                .currentLatitude(currentLatitude)
                .currentLongitude(currentLongitude)
                .registeredLatitude(registeredLocation.getLatitude())
                .registeredLongitude(registeredLocation.getLongitude())
                .distanceFromRegistered(distance)
                .isWithinTolerance(isWithinTolerance)
                .result(result)
                .evidence(evidence)
                .verifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radio de la Tierra en kilómetros

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // Convertir a metros
    }

    /**
     * Verifica si una ubicación es válida para enviar evidencia
     */
    public boolean isLocationValidForEvidence(Evidence evidence, Double latitude, Double longitude) {
        try {
            EvidenceLocationVerification verification = verifyLocationForEvidence(evidence, latitude, longitude);
            return verification.getResult() == LocationVerificationResult.VERIFIED;
        } catch (Exception e) {
            log.error("Error verificando ubicación para evidencia: {}", e.getMessage());
            return false;
        }
    }
} 