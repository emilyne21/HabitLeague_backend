package com.example.habitleague.challenge.model;

import com.example.habitleague.location.model.RegisteredLocation;
import com.example.habitleague.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "challenge_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate joinedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer progressDays = 0;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalPenalties = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paymentCompleted = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean locationRegistered = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasCompleted = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @OneToOne(mappedBy = "challengeMember", cascade = CascadeType.ALL)
    private RegisteredLocation registeredLocation;
} 