package com.example.habitleague.evidence.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitEvidenceRequest {
    
    @NotNull(message = "El ID del reto es requerido")
    private Long challengeId;

    @NotNull(message = "La URL de la imagen es requerida")
    private String imageUrl;

    @NotNull(message = "La latitud es requerida")
    @DecimalMin(value = "-90", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90", message = "La latitud debe estar entre -90 y 90")
    private Double latitude;

    @NotNull(message = "La longitud es requerida")
    @DecimalMin(value = "-180", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180", message = "La longitud debe estar entre -180 y 180")
    private Double longitude;
} 