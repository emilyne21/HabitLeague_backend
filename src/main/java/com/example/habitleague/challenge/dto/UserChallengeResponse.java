package com.example.habitleague.challenge.dto;

import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeCategory;
import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.challenge.model.ChallengeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class UserChallengeResponse {
    // Información del challenge
    private Long challengeId;
    private String name;
    private String description;
    private ChallengeCategory category;
    private String imageUrl;
    private String rules;
    private Integer durationDays;
    private BigDecimal entryFee;
    private Boolean featured;
    private LocalDate startDate;
    private LocalDate endDate;
    private ChallengeStatus status;
    private Integer participantCount;
    
    // Información de la membresía del usuario
    private Long membershipId;
    private LocalDate joinedAt;
    private Integer progressDays;
    private BigDecimal totalPenalties;
    private Boolean paymentCompleted;
    private Boolean locationRegistered;
    private Boolean hasCompleted;
    
    // Información del creador
    private String creatorName;
    private String creatorEmail;

    public static UserChallengeResponse fromChallengeMember(ChallengeMember member) {
        Challenge challenge = member.getChallenge();
        
        return UserChallengeResponse.builder()
                // Información del challenge
                .challengeId(challenge.getId())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .category(challenge.getCategory())
                .imageUrl(challenge.getImageUrl())
                .rules(challenge.getRules())
                .durationDays(challenge.getDurationDays())
                .entryFee(challenge.getEntryFee())
                .featured(challenge.getFeatured())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .status(challenge.getStatus())
                .participantCount(challenge.getMembers() != null ? challenge.getMembers().size() : 0)
                
                // Información de la membresía
                .membershipId(member.getId())
                .joinedAt(member.getJoinedAt())
                .progressDays(member.getProgressDays())
                .totalPenalties(member.getTotalPenalties())
                .paymentCompleted(member.getPaymentCompleted())
                .locationRegistered(member.getLocationRegistered())
                .hasCompleted(member.getHasCompleted())
                
                // Información del creador
                .creatorName(challenge.getCreatedBy().getFirstName() + " " + challenge.getCreatedBy().getLastName())
                .creatorEmail(challenge.getCreatedBy().getEmail())
                .build();
    }
} 