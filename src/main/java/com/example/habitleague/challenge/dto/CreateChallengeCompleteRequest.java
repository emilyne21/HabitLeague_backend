package com.example.habitleague.challenge.dto;

import com.example.habitleague.challenge.model.ChallengeCategory;
import com.example.habitleague.location.dto.CreateLocationRegistrationRequest;
import com.example.habitleague.payment.dto.CreatePaymentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChallengeCompleteRequest {
    
    // Información del challenge
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String description;
    
    @NotNull(message = "La categoría es obligatoria")
    private ChallengeCategory category;
    
    private String imageUrl;
    
    private String rules;
    
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 21, message = "La duración mínima es 21 días")
    @Max(value = 365, message = "La duración máxima es 365 días")
    private Integer durationDays;
    
    @NotNull(message = "La tarifa de entrada es obligatoria")
    @DecimalMin(value = "0.01", message = "La tarifa debe ser mayor a 0")
    private BigDecimal entryFee;
        
    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser presente o futura")
    private LocalDate startDate;
    
    @NotNull(message = "La fecha de fin es obligatoria")
    @FutureOrPresent(message = "La fecha de fin debe ser presente o futura")
    private LocalDate endDate;

    // Información de pago del creador - Usando DTO sin challengeId obligatorio
    @NotNull(message = "La información de pago es obligatoria")
    @Valid
    private CreatePaymentRequest payment;

    // Información de ubicación del creador - Usando DTO sin challengeId obligatorio
    @NotNull(message = "La información de ubicación es obligatoria")
    @Valid
    private CreateLocationRegistrationRequest location;
} 