package com.example.habitleague.payment.controller;

import com.example.habitleague.payment.dto.PaymentRequest;
import com.example.habitleague.payment.dto.PaymentResponse;
import com.example.habitleague.payment.service.PaymentService;
import com.example.habitleague.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Transactional
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestBody @Valid PaymentRequest request,
            @AuthenticationPrincipal User user) {
        PaymentResponse response = paymentService.processPayment(request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process-penalty")
    @Transactional
    public ResponseEntity<PaymentResponse> processPenaltyPayment(
            @RequestBody @Valid PaymentRequest request,
            @AuthenticationPrincipal User user) {
        PaymentResponse response = paymentService.processPenaltyPayment(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-payments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @AuthenticationPrincipal User user) {
        List<PaymentResponse> payments = paymentService.getUserPayments(user);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/stripe/{stripePaymentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<PaymentResponse> getPaymentByStripeId(
            @PathVariable String stripePaymentId) {
        PaymentResponse payment = paymentService.getPaymentByStripeId(stripePaymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/challenge/{challengeId}/status")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPaymentStatus(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal User user) {
        
        // Este endpoint será útil para verificar si el usuario ya pagó por un challenge específico
        // Aquí podrías agregar lógica adicional para obtener el challenge y verificar
        boolean hasPaid = paymentService.hasUserPaidForChallenge(user, null); // Necesitarías obtener el challenge
        
        return ResponseEntity.ok(Map.of(
            "hasPaid", hasPaid,
            "userId", user.getId(),
            "challengeId", challengeId
        ));
    }

    // Endpoint para simular webhook de Stripe (solo para desarrollo)
    @PostMapping("/webhook/stripe")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody Map<String, Object> payload) {
        
        // En producción, aquí manejarías los webhooks reales de Stripe
        // Por ahora, solo simularemos la respuesta
        return ResponseEntity.ok(Map.of(
            "message", "Webhook procesado correctamente",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
} 