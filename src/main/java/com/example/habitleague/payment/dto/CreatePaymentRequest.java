package com.example.habitleague.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    
    // El challengeId será asignado automáticamente por el servicio
    private Long challengeId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    private String currency = "USD"; // Moneda por defecto

    // Datos simulados de Stripe
    private String paymentMethodId; // ID del método de pago simulado
    private String cardLast4 = "4242"; // Últimos 4 dígitos simulados
    private String cardBrand = "visa"; // Marca de tarjeta simulada

    /**
     * Convierte este DTO a PaymentRequest asignando el challengeId
     */
    public PaymentRequest toPaymentRequest(Long challengeId) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setChallengeId(challengeId);
        paymentRequest.setAmount(this.amount);
        paymentRequest.setCurrency(this.currency);
        paymentRequest.setPaymentMethodId(this.paymentMethodId);
        paymentRequest.setCardLast4(this.cardLast4);
        paymentRequest.setCardBrand(this.cardBrand);
        return paymentRequest;
    }
} 