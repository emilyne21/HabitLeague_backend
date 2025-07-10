package com.example.habitleague.challenge.dto;

import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ChallengeSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private ChallengeCategory category;
    private String imageUrl;
    private Integer durationDays;
    private BigDecimal entryFee;
    private Integer participantCount;
    private Boolean featured;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Campos de ubicación
    private Double latitude;
    private Double longitude;
    private String address;
    private String locationName;
    private Double toleranceRadius;

    public static ChallengeSummaryResponse fromChallenge(Challenge challenge) {
        return ChallengeSummaryResponse.builder()
                .id(challenge.getId())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .category(challenge.getCategory())
                .imageUrl(challenge.getImageUrl())
                .durationDays(challenge.getDurationDays())
                .entryFee(challenge.getEntryFee())
                .participantCount(challenge.getMembers() != null ? challenge.getMembers().size() : 0)
                .featured(challenge.getFeatured())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .build();
    }
    
    public static ChallengeSummaryResponse fromChallengeWithLocation(Challenge challenge, 
            Double latitude, Double longitude, String address, String locationName, Double toleranceRadius) {
        return ChallengeSummaryResponse.builder()
                .id(challenge.getId())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .category(challenge.getCategory())
                .imageUrl(challenge.getImageUrl())
                .durationDays(challenge.getDurationDays())
                .entryFee(challenge.getEntryFee())
                .participantCount(challenge.getMembers() != null ? challenge.getMembers().size() : 0)
                .featured(challenge.getFeatured())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .locationName(locationName)
                .toleranceRadius(toleranceRadius)
                .build();
    }
} 