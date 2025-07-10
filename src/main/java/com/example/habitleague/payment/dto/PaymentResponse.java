package com.example.habitleague.payment.dto;

import com.example.habitleague.payment.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    
    private Long paymentId;
    private String stripePaymentId;
    private String stripeSessionId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String challengeName;
    private Long challengeId;
} 