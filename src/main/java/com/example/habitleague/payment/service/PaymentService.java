package com.example.habitleague.payment.service;

import com.example.habitleague.achievement.service.AchievementEvaluationService;
import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.repository.ChallengeRepository;
import com.example.habitleague.payment.dto.PaymentRequest;
import com.example.habitleague.payment.dto.PaymentResponse;
import com.example.habitleague.payment.model.Payment;
import com.example.habitleague.payment.model.PaymentStatus;
import com.example.habitleague.payment.repository.PaymentRepository;
import com.example.habitleague.shared.exception.ChallengeException;
import com.example.habitleague. user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ChallengeRepository challengeRepository;
    private final AchievementEvaluationService achievementEvaluationService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, User user) {
        log.info("Procesando pago para usuario {} y challenge {}", user.getEmail(), request.getChallengeId());

        // Verificar que el challenge existe
        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ChallengeException("Challenge no encontrado"));

        // Verificar que el usuario no haya pagado ya por este challenge
        boolean alreadyPaid = paymentRepository.existsByUserAndChallengeAndStatus(
                user, challenge, PaymentStatus.SUCCEEDED);
        if (alreadyPaid) {
            throw new ChallengeException("Ya has pagado por este challenge");
        }

        // Simular procesamiento con Stripe
        Payment payment = simulateStripePayment(request, user, challenge);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Pago procesado exitosamente con ID: {}", savedPayment.getId());

        return PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .stripePaymentId(savedPayment.getStripePaymentId())
                .stripeSessionId(savedPayment.getStripeSessionId())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .status(savedPayment.getStatus())
                .createdAt(savedPayment.getCreatedAt())
                .processedAt(savedPayment.getProcessedAt())
                .challengeName(challenge.getName())
                .challengeId(challenge.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(User user) {
        List<Payment> payments = paymentRepository.findByUser(user);
        return payments.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByStripeId(String stripePaymentId) {
        Payment payment = paymentRepository.findByStripePaymentId(stripePaymentId)
                .orElseThrow(() -> new ChallengeException("Pago no encontrado"));
        return convertToResponse(payment);
    }

    @Transactional(readOnly = true)
    public boolean hasUserPaidForChallenge(User user, Challenge challenge) {
        return paymentRepository.existsByUserAndChallengeAndStatus(
                user, challenge, PaymentStatus.SUCCEEDED);
    }

    /**
     * Procesa un pago de penalización cuando un usuario no completa un reto
     */
    @Transactional
    public PaymentResponse processPenaltyPayment(PaymentRequest request, User user) {
        log.info("Procesando pago de penalización para usuario {} y challenge {}", 
            user.getEmail(), request.getChallengeId());

        // Verificar que el challenge existe
        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ChallengeException("Challenge no encontrado"));

        // Simular procesamiento con Stripe
        Payment payment = simulateStripePayment(request, user, challenge);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // ✅ EVALUACIÓN DE LOGRO: Primer pago de penalización
        if (savedPayment.getStatus() == PaymentStatus.SUCCEEDED) {
            achievementEvaluationService.evaluateFirstPenaltyPayment(
                user.getId(), 
                challenge.getId()
            );
        }
        
        log.info("Pago de penalización procesado exitosamente con ID: {}", savedPayment.getId());

        return PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .stripePaymentId(savedPayment.getStripePaymentId())
                .stripeSessionId(savedPayment.getStripeSessionId())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .status(savedPayment.getStatus())
                .createdAt(savedPayment.getCreatedAt())
                .processedAt(savedPayment.getProcessedAt())
                .challengeName(challenge.getName())
                .challengeId(challenge.getId())
                .build();
    }

    private Payment simulateStripePayment(PaymentRequest request, User user, Challenge challenge) {
        // Simular procesamiento de Stripe
        String stripePaymentId = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String stripeSessionId = "cs_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        
        // Simular 95% de éxito, 5% de fallo
        boolean isSuccessful = Math.random() < 0.95;
        PaymentStatus status = isSuccessful ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
        
        LocalDateTime now = LocalDateTime.now();
        
        return Payment.builder()
                .stripePaymentId(stripePaymentId)
                .stripeSessionId(stripeSessionId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(status)
                .createdAt(now)
                .processedAt(isSuccessful ? now : null)
                .user(user)
                .challenge(challenge)
                .build();
    }

    private PaymentResponse convertToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .stripePaymentId(payment.getStripePaymentId())
                .stripeSessionId(payment.getStripeSessionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .challengeName(payment.getChallenge().getName())
                .challengeId(payment.getChallenge().getId())
                .build();
    }
} 