package com.example.habitleague.challenge.service;

import com.example.habitleague.achievement.service.AchievementEvaluationService;
import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.challenge.model.DailyEvidenceCheck;
import com.example.habitleague.challenge.model.PrizeDistribution;
import com.example.habitleague.challenge.repository.ChallengeMemberRepository;
import com.example.habitleague.challenge.repository.ChallengeRepository;
import com.example.habitleague.challenge.repository.DailyEvidenceCheckRepository;
import com.example.habitleague.challenge.repository.PrizeDistributionRepository;
import com.example.habitleague.evidence.repository.EvidenceRepository;
import com.example.habitleague.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChallengeLifecycleService {
    
    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository memberRepository;
    private final EvidenceRepository evidenceRepository;
    private final DailyEvidenceCheckRepository checkRepository;
    private final PrizeDistributionRepository prizeRepository;
    private final PaymentService paymentService;
    private final AchievementEvaluationService achievementEvaluationService;
    
    /**
     * Método principal que se ejecuta diariamente para verificar evidencias
     * y gestionar el ciclo de vida de los challenges
     */
    public void performDailyLifecycleCheck() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Iniciando verificación diaria para fecha: {}", yesterday);
        
        List<Challenge> activeChallenges = challengeRepository.findActiveChallengesForDate(yesterday);
        log.info("Encontrados {} challenges activos", activeChallenges.size());
        
        for (Challenge challenge : activeChallenges) {
            try {
                processChallenge(challenge, yesterday);
            } catch (Exception e) {
                log.error("Error procesando challenge {}: {}", challenge.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Verificación diaria completada");
    }
    
    /**
     * Procesa un challenge específico: verifica evidencias, actualiza pricepool 
     * y distribuye premios si es necesario
     */
    private void processChallenge(Challenge challenge, LocalDate checkDate) {
        log.info("Procesando challenge: {} - {}", challenge.getId(), challenge.getName());
        
        // Verificar si ya se realizó el check para esta fecha
        if (checkRepository.existsByChallengeIdAndCheckDate(challenge.getId(), checkDate)) {
            log.info("Check ya realizado para challenge {} en fecha {}", challenge.getId(), checkDate);
            return;
        }
        
        // 1. Verificar evidencias del día anterior
        int eliminated = checkDailyEvidences(challenge, checkDate);
        
        // 2. Actualizar pricepool
        updatePricepool(challenge);
        
        // 3. Si el challenge terminó, distribuir premios
        if (challenge.getEndDate().equals(checkDate)) {
            distributePrizes(challenge);
        }
        
        // 4. Guardar registro de auditoría
        saveDailyCheck(challenge, checkDate, eliminated);
        
        log.info("Challenge {} procesado exitosamente", challenge.getId());
    }
    
    /**
     * Verifica las evidencias del día anterior y elimina a participantes sin evidencia
     */
    private int checkDailyEvidences(Challenge challenge, LocalDate checkDate) {
        List<ChallengeMember> activeMembers = memberRepository
            .findByChallengeAndHasCompletedTrue(challenge);
            
        int eliminated = 0;
        LocalDateTime startOfDay = checkDate.atStartOfDay();
        LocalDateTime endOfDay = checkDate.atTime(23, 59, 59);
        
        log.info("Verificando evidencias para {} participantes activos", activeMembers.size());
        
        for (ChallengeMember member : activeMembers) {
            boolean hasEvidence = evidenceRepository
                .existsByChallengeMemberAndSubmittedAtBetween(
                    member, startOfDay, endOfDay);
                    
            if (!hasEvidence) {
                // Eliminar participante por falta de evidencia
                member.setHasCompleted(false);
                memberRepository.save(member);
                eliminated++;
                
                log.warn("Eliminado participante {} del challenge {} por falta de evidencia en {}", 
                    member.getUser().getEmail(), challenge.getId(), checkDate);
            } else {
                // Incrementar progreso del participante
                member.setProgressDays(member.getProgressDays() + 1);
                memberRepository.save(member);
                
                log.debug("Participante {} mantiene progreso: {} días", 
                    member.getUser().getEmail(), member.getProgressDays());
                
                // ✅ EVALUACIÓN DE LOGRO: Racha de 7 días
                achievementEvaluationService.evaluateSevenDayStreak(
                    member.getUser().getId(), 
                    member.getProgressDays(), 
                    challenge.getId()
                );
            }
        }
        
        log.info("Verificación completada: {} eliminados de {} participantes", 
            eliminated, activeMembers.size());
        return eliminated;
    }
    
    /**
     * Actualiza el pricepool del challenge con el número actual de participantes activos
     */
    private void updatePricepool(Challenge challenge) {
        // Calcular pricepool total si no existe
        if (challenge.getTotalPricepool() == null) {
            List<ChallengeMember> paidMembers = memberRepository
                .findByChallengeAndPaymentCompletedTrue(challenge);
            
            BigDecimal total = paidMembers.stream()
                .map(member -> challenge.getEntryFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            challenge.setTotalPricepool(total);
            log.info("Pricepool inicial calculado para challenge {}: ${}", 
                challenge.getId(), total);
        }
        
        // Actualizar participantes activos
        int activeCount = memberRepository.countByChallengeAndHasCompletedTrue(challenge);
        challenge.setActiveParticipants(activeCount);
        
        challengeRepository.save(challenge);
        
        log.info("Pricepool actualizado - Challenge {}: ${} total, {} participantes activos", 
            challenge.getId(), challenge.getTotalPricepool(), activeCount);
    }
    
    /**
     * Distribuye los premios entre los participantes que completaron el challenge
     */
    private void distributePrizes(Challenge challenge) {
        if (challenge.getPrizesDistributed()) {
            log.info("Premios ya distribuidos para challenge {}", challenge.getId());
            return;
        }
        
        List<ChallengeMember> winners = memberRepository
            .findByChallengeAndHasCompletedTrue(challenge);
            
        if (winners.isEmpty()) {
            log.warn("No hay ganadores para el challenge {}", challenge.getId());
            challenge.setPrizesDistributed(true);
            challengeRepository.save(challenge);
            return;
        }
        
        BigDecimal prizePerWinner = challenge.getTotalPricepool()
            .divide(BigDecimal.valueOf(winners.size()), 2, RoundingMode.HALF_UP);
            
        log.info("Distribuyendo premios - Challenge {}: {} ganadores, ${} cada uno", 
            challenge.getId(), winners.size(), prizePerWinner);
            
        for (ChallengeMember winner : winners) {
            try {
                // ✅ EVALUACIÓN DE LOGROS: Primer reto completado y Sin excusas
                achievementEvaluationService.evaluateFirstChallengeCompleted(
                    winner.getUser().getId(), 
                    challenge.getId()
                );
                
                achievementEvaluationService.evaluatePerfectChallenge(
                    winner.getUser().getId(),
                    challenge.getId(),
                    winner.getProgressDays(),
                    challenge.getDurationDays()
                );
                
                // Crear registro de distribución
                PrizeDistribution distribution = PrizeDistribution.builder()
                    .challengeMemberId(winner.getId())
                    .challengeId(challenge.getId())
                    .prizeAmount(prizePerWinner)
                    .build();
                    
                prizeRepository.save(distribution);
                
                // Procesar pago del premio (simulado)
                String transactionId = processWinnerPayout(winner, prizePerWinner, challenge);
                
                if (transactionId != null) {
                    distribution.setPaid(true);
                    distribution.setPaymentTransactionId(transactionId);
                    distribution.setPaidAt(LocalDateTime.now());
                    prizeRepository.save(distribution);
                    
                    log.info("Premio pagado exitosamente a {}: ${}", 
                        winner.getUser().getEmail(), prizePerWinner);
                }
                
            } catch (Exception e) {
                log.error("Error pagando premio a {}: {}", 
                    winner.getUser().getEmail(), e.getMessage());
            }
        }
        
        challenge.setPrizesDistributed(true);
        challengeRepository.save(challenge);
        
        log.info("Distribución de premios completada para challenge {}", challenge.getId());
    }
    
    /**
     * Procesa el pago del premio a un ganador (simulado)
     */
    private String processWinnerPayout(ChallengeMember winner, BigDecimal amount, Challenge challenge) {
        try {
            // En un sistema real, aquí se integraría con Stripe para enviar dinero
            // Por ahora, simulamos el pago exitoso
            String transactionId = "payout_" + System.currentTimeMillis() + "_" + winner.getId();
            
            log.info("Simulando pago de premio: ${} para {} (transactionId: {})", 
                amount, winner.getUser().getEmail(), transactionId);
            
            return transactionId;
        } catch (Exception e) {
            log.error("Error procesando pago de premio: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Guarda un registro de auditoría del check diario
     */
    private void saveDailyCheck(Challenge challenge, LocalDate checkDate, int eliminated) {
        int activeRemaining = memberRepository.countByChallengeAndHasCompletedTrue(challenge);
        
        DailyEvidenceCheck check = DailyEvidenceCheck.builder()
            .challengeId(challenge.getId())
            .checkDate(checkDate)
            .participantsEliminated(eliminated)
            .activeRemaining(activeRemaining)
            .build();
            
        checkRepository.save(check);
        
        log.info("Registro de auditoría guardado para challenge {} - fecha {}: {} eliminados, {} activos", 
            challenge.getId(), checkDate, eliminated, activeRemaining);
    }
    
    /**
     * Método público para inicializar el pricepool de un challenge
     * (se llama cuando se crea un challenge o cuando alguien se une)
     */
    @Transactional
    public void initializePricepool(Challenge challenge) {
        if (challenge.getTotalPricepool() == null) {
            updatePricepool(challenge);
        }
    }
    
    /**
     * Método público para obtener estadísticas del pricepool
     */
    @Transactional(readOnly = true)
    public PricepoolStats getPricepoolStats(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new RuntimeException("Challenge no encontrado"));
            
        int activeParticipants = challenge.getActiveParticipants() != null ? 
            challenge.getActiveParticipants() : 0;
            
        BigDecimal totalPrizepool = challenge.getTotalPricepool() != null ? 
            challenge.getTotalPricepool() : BigDecimal.ZERO;
            
        BigDecimal prizePerWinner = activeParticipants > 0 ? 
            totalPrizepool.divide(BigDecimal.valueOf(activeParticipants), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
            
        return new PricepoolStats(
            challengeId,
            totalPrizepool,
            activeParticipants,
            prizePerWinner,
            challenge.getPrizesDistributed() != null ? challenge.getPrizesDistributed() : false
        );
    }
    
    /**
     * Clase para estadísticas del pricepool
     */
    public static class PricepoolStats {
        public final Long challengeId;
        public final BigDecimal totalPrizepool;
        public final Integer activeParticipants;
        public final BigDecimal prizePerWinner;
        public final Boolean prizesDistributed;
        
        public PricepoolStats(Long challengeId, BigDecimal totalPrizepool, 
                            Integer activeParticipants, BigDecimal prizePerWinner, 
                            Boolean prizesDistributed) {
            this.challengeId = challengeId;
            this.totalPrizepool = totalPrizepool;
            this.activeParticipants = activeParticipants;
            this.prizePerWinner = prizePerWinner;
            this.prizesDistributed = prizesDistributed;
        }
    }
} 