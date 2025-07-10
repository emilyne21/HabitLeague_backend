package com.example.habitleague.challenge.dto;

import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeCategory;
import com.example.habitleague.challenge.model.ChallengeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChallengeResponse {
    private Long id;
    private String name;
    private String description;
    
    // Nuevos campos
    private ChallengeCategory category;
    private String imageUrl;
    private String rules;
    private Integer durationDays;
    private BigDecimal entryFee;
    private Boolean featured;
    private Integer participantCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private ChallengeStatus status;
    private String creatorName;
    private String creatorEmail;

    public static ChallengeResponse fromChallenge(Challenge challenge) {
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .category(challenge.getCategory())
                .imageUrl(challenge.getImageUrl())
                .rules(challenge.getRules())
                .durationDays(challenge.getDurationDays())
                .entryFee(challenge.getEntryFee())
                .featured(challenge.getFeatured())
                .participantCount(challenge.getMembers() != null ? challenge.getMembers().size() : 1)
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .status(challenge.getStatus())
                .creatorName(challenge.getCreatedBy().getFirstName() + " " + challenge.getCreatedBy().getLastName())
                .creatorEmail(challenge.getCreatedBy().getEmail())
                .build();
    }
} 