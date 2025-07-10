package com.example.habitleague.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    
    @NotNull(message = "El ID del challenge es obligatorio")
    private Long challengeId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    private String currency = "USD"; // Moneda por defecto

    // Datos simulados de Stripe
    private String paymentMethodId; // ID del método de pago simulado
    private String cardLast4 = "4242"; // Últimos 4 dígitos simulados
    private String cardBrand = "visa"; // Marca de tarjeta simulada
} 