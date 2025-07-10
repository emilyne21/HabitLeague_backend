package com.example.habitleague.location.controller;

import com.example.habitleague.location.dto.LocationRegistrationResponse;
import com.example.habitleague.location.service.LocationRegistrationService;
import com.example.habitleague.shared.exception.ChallengeException;
import com.example.habitleague.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.habitleague.location.model.RegisteredLocation;
import java.util.Optional;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationRegistrationService locationRegistrationService;

    @GetMapping("/my-registrations")
    @Transactional(readOnly = true)
    public ResponseEntity<List<LocationRegistrationResponse>> getMyRegistrations(
            @AuthenticationPrincipal User user) {
        try {
            List<LocationRegistrationResponse> registrations = 
                    locationRegistrationService.getUserRegisteredLocations(user.getId());
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            log.error("Error obteniendo ubicaciones registradas para usuario {}: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/challenge/{challengeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<LocationRegistrationResponse> getRegistrationByChallenge(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal User user) {
        
        log.debug("Solicitando ubicación para challenge {} y usuario {}", challengeId, user.getEmail());
        
        try {
            LocationRegistrationResponse response = 
                    locationRegistrationService.getRegistrationByUserAndChallenge(user.getId(), challengeId);
            
            log.debug("Ubicación encontrada para challenge {}: {}", challengeId, response.getLocationName());
            return ResponseEntity.ok(response);
            
        } catch (ChallengeException e) {
            log.warn("Ubicación no encontrada para challenge {} y usuario {}: {}", 
                    challengeId, user.getEmail(), e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error interno obteniendo ubicación registrada para challenge {} y usuario {}: {}", 
                    challengeId, user.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/challenge/{challengeId}/creator")
    @Transactional(readOnly = true)
    public ResponseEntity<LocationRegistrationResponse> getCreatorLocationByChallenge(
            @PathVariable Long challengeId) {
        
        log.debug("Solicitando ubicación del creador para challenge {}", challengeId);
        
        try {
            Optional<RegisteredLocation> creatorLocation = 
                    locationRegistrationService.getCreatorLocationByChallenge(challengeId);
            
            if (creatorLocation.isPresent()) {
                LocationRegistrationResponse response = 
                        locationRegistrationService.convertToResponse(creatorLocation.get());
                log.debug("Ubicación del creador encontrada para challenge {}: {}", 
                        challengeId, response.getLocationName());
                return ResponseEntity.ok(response);
            } else {
                log.warn("No se encontró ubicación del creador para challenge {}", challengeId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error interno obteniendo ubicación del creador para challenge {}: {}", 
                    challengeId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint simulado para obtener información de ubicación desde Google Maps
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> simulateGeocode(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        // Simular respuesta de Google Maps Geocoding API
        Map<String, Object> geocodeResponse = Map.of(
            "latitude", latitude,
            "longitude", longitude,
            "address", "Dirección simulada para lat: " + latitude + ", lng: " + longitude,
            "city", "Ciudad Simulada",
            "country", "México",
            "accuracy", "ROOFTOP",
            "placeId", "simulated_place_id_" + System.currentTimeMillis()
        );

        return ResponseEntity.ok(geocodeResponse);
    }

    // Endpoint para verificar si una ubicación está cerca de otra (simulado)
    // Esto se usará cuando se envíen evidencias para verificar que el usuario está en la ubicación registrada
    @PostMapping("/verify-proximity")
    public ResponseEntity<Map<String, Object>> verifyProximity(
            @RequestParam Double userLat,
            @RequestParam Double userLng,
            @RequestParam Double targetLat,
            @RequestParam Double targetLng,
            @RequestParam(defaultValue = "100") Double radiusMeters) {

        // Calcular distancia simulada (fórmula haversine simplificada)
        double distance = calculateDistance(userLat, userLng, targetLat, targetLng);
        boolean isWithinRadius = distance <= radiusMeters;

        Map<String, Object> response = Map.of(
            "isWithinRadius", isWithinRadius,
            "distance", distance,
            "radiusMeters", radiusMeters,
            "message", isWithinRadius ? 
                "Estás en la ubicación correcta para enviar evidencia" : 
                "Debes estar en la ubicación registrada para enviar evidencia",
            "userLocation", Map.of("lat", userLat, "lng", userLng),
            "targetLocation", Map.of("lat", targetLat, "lng", targetLng)
        );

        return ResponseEntity.ok(response);
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Simulación simple de cálculo de distancia
        // En producción usarías una implementación real de la fórmula haversine
        double deltaLat = Math.abs(lat1 - lat2);
        double deltaLng = Math.abs(lng1 - lng2);
        return (deltaLat + deltaLng) * 111000; // Aproximación muy básica en metros
    }
} 