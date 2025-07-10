package com.example.habitleague.challenge.model;

import com.example.habitleague.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    // Nuevos campos
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeCategory category;

    @Column
    private String imageUrl;

    @Column(length = 1000)
    private String rules;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private BigDecimal entryFee;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean featured = false;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;

    // Campos del pricepool
    @Column(precision = 19, scale = 2)
    private BigDecimal totalPricepool;

    @Column
    private Integer activeParticipants;

    @Column
    @Builder.Default
    private Boolean prizesDistributed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<ChallengeMember> members;
} 