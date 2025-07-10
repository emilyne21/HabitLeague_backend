package com.example.habitleague.challenge.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_evidence_checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyEvidenceCheck {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private LocalDate checkDate;

    @Column(nullable = false)
    private Integer participantsEliminated;

    @Column(nullable = false)
    private Integer activeRemaining;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkedAt;
} 