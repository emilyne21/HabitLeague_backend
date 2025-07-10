package com.example.habitleague.payment.repository;

import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.payment.model.Payment;
import com.example.habitleague.payment.model.PaymentStatus;
import com.example.habitleague.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByUser(User user);
    
    List<Payment> findByChallenge(Challenge challenge);
    
    List<Payment> findByUserAndChallenge(User user, Challenge challenge);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    Optional<Payment> findByStripePaymentId(String stripePaymentId);
    
    Optional<Payment> findByStripeSessionId(String stripeSessionId);
    
    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.challenge = :challenge AND p.status = :status")
    Optional<Payment> findByUserAndChallengeAndStatus(
        @Param("user") User user, 
        @Param("challenge") Challenge challenge, 
        @Param("status") PaymentStatus status
    );
    
    boolean existsByUserAndChallengeAndStatus(User user, Challenge challenge, PaymentStatus status);
} 