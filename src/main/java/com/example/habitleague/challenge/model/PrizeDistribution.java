package com.example.habitleague.challenge.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prize_distributions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrizeDistribution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeMemberId;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal prizeAmount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column
    private String paymentTransactionId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime paidAt;
} 