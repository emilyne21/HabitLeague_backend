package com.example.habitleague.location.service;

import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.challenge.repository.ChallengeMemberRepository;
import com.example.habitleague.location.dto.LocationRegistrationRequest;
import com.example.habitleague.location.dto.LocationRegistrationResponse;
import com.example.habitleague.location.model.RegisteredLocation;
import com.example.habitleague.location.repository.RegisteredLocationRepository;
import com.example.habitleague.shared.exception.ChallengeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationRegistrationService {

    private final RegisteredLocationRepository registeredLocationRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final Random random = new Random();

    // Direcciones simuladas para diferentes coordenadas
    private final String[] SAMPLE_ADDRESSES = {
        "123 Main Street, Ciudad de México, CDMX",
        "456 Oak Avenue, Guadalajara, Jalisco", 
        "789 Pine Boulevard, Monterrey, Nuevo León",
        "321 Elm Drive, Puebla, Puebla",
        "654 Maple Lane, Tijuana, Baja California"
    };

    private final String[] SAMPLE_LOCATION_NAMES = {
        "Gimnasio Central", "Parque Nacional", "Centro Deportivo", 
        "Plaza Principal", "Complexo Atlético", "Zona Verde",
        "Centro Comercial", "Área Recreativa"
    };

    @Transactional
    public LocationRegistrationResponse registerLocation(LocationRegistrationRequest request, ChallengeMember challengeMember) {
        log.info("Registrando ubicación para miembro {} en challenge {}", 
                challengeMember.getUser().getEmail(), request.getChallengeId());

        // Verificar que no haya ya una ubicación registrada para este miembro
        if (registeredLocationRepository.findByChallengeMember(challengeMember).isPresent()) {
            throw new ChallengeException("Ya existe una ubicación registrada para este challenge");
        }

        // Simular obtención de dirección desde Google Maps
        RegisteredLocation location = simulateLocationRegistration(request, challengeMember);
        
        RegisteredLocation savedLocation = registeredLocationRepository.save(location);
        
        // Actualizar el estado del miembro
        challengeMember.setLocationRegistered(true);
        challengeMemberRepository.save(challengeMember);
        
        log.info("Ubicación registrada exitosamente con ID: {}", savedLocation.getId());

        return convertToResponse(savedLocation);
    }

    @Transactional(readOnly = true)
    public List<LocationRegistrationResponse> getUserRegisteredLocations(Long userId) {
        List<RegisteredLocation> locations = registeredLocationRepository.findByUserId(userId);
        return locations.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocationRegistrationResponse getRegistrationByUserAndChallenge(Long userId, Long challengeId) {
        try {
            RegisteredLocation location = registeredLocationRepository
                    .findByUserIdAndChallengeId(userId, challengeId)
                    .orElseThrow(() -> new ChallengeException("Ubicación registrada no encontrada"));
            return convertToResponse(location);
        } catch (ChallengeException e) {
            // Re-lanzar ChallengeException para manejo específico en el controlador
            throw e;
        } catch (Exception e) {
            // Log del error y re-lanzar como ChallengeException para consistencia
            log.error("Error inesperado obteniendo ubicación para usuario {} y challenge {}: {}", 
                    userId, challengeId, e.getMessage(), e);
            throw new ChallengeException("Error interno obteniendo ubicación registrada");
        }
    }

    @Transactional(readOnly = true)
    public RegisteredLocation getRegisteredLocationByChallengeMember(ChallengeMember challengeMember) {
        return registeredLocationRepository.findByChallengeMember(challengeMember)
                .orElseThrow(() -> new ChallengeException("No hay ubicación registrada para este miembro"));
    }

    private RegisteredLocation simulateLocationRegistration(LocationRegistrationRequest request, ChallengeMember challengeMember) {
        // Simular obtención de dirección desde Google Maps
        String simulatedAddress = SAMPLE_ADDRESSES[random.nextInt(SAMPLE_ADDRESSES.length)];
        
        // Usar el nombre proporcionado o generar uno aleatorio
        String locationName = request.getLocationName() != null ? 
            request.getLocationName() : 
            SAMPLE_LOCATION_NAMES[random.nextInt(SAMPLE_LOCATION_NAMES.length)];

        // Usar radio de tolerancia proporcionado o default de 100m
        Double toleranceRadius = request.getToleranceRadius() != null ? 
            request.getToleranceRadius() : 
            100.0;

        return RegisteredLocation.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(simulatedAddress)
                .locationName(locationName)
                .registeredAt(LocalDateTime.now())
                .toleranceRadius(toleranceRadius)
                .challengeMember(challengeMember)
                .build();
    }

    private LocationRegistrationResponse convertToResponse(RegisteredLocation location) {
        return LocationRegistrationResponse.builder()
                .registrationId(location.getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .locationName(location.getLocationName())
                .registeredAt(location.getRegisteredAt())
                .toleranceRadius(location.getToleranceRadius())
                .challengeName(location.getChallengeMember().getChallenge().getName())
                .challengeId(location.getChallengeMember().getChallenge().getId())
                .build();
    }
} 