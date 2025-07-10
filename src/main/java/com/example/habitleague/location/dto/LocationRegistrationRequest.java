package com.example.habitleague.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationRegistrationRequest {
    
    @NotNull(message = "El ID del challenge es obligatorio")
    private Long challengeId;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitude;

    private String address; // Dirección real proporcionada por el usuario
    private String locationName; // Nombre del lugar proporcionado por el usuario
    private Double toleranceRadius; // Radio de tolerancia en metros (default: 100m)
} 