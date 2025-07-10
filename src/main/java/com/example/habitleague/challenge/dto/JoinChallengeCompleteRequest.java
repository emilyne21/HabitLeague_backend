package com.example.habitleague.challenge.dto;

import com.example.habitleague.location.dto.LocationRegistrationRequest;
import com.example.habitleague.payment.dto.PaymentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinChallengeCompleteRequest {
    
    @NotNull(message = "La información de pago es obligatoria")
    @Valid
    private PaymentRequest payment;

    @NotNull(message = "La información de ubicación es obligatoria")
    @Valid
    private LocationRegistrationRequest location;
} 