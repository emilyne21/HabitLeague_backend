package com.example.habitleague.evidence.dto;

import com.example.habitleague.evidence.model.Evidence;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EvidenceResponse {
    private Long id;
    private String imageUrl;
    private Boolean aiValidated;
    private Double latitude;
    private Double longitude;
    private Boolean locationValid;
    private LocalDateTime submittedAt;
    private Long challengeId;
    private String challengeName;

    public static EvidenceResponse fromEvidence(Evidence evidence) {
        return EvidenceResponse.builder()
                .id(evidence.getId())
                .imageUrl(evidence.getImageUrl())
                .aiValidated(evidence.getAiValidated())
                .latitude(evidence.getLatitude())
                .longitude(evidence.getLongitude())
                .locationValid(evidence.getLocationValid())
                .submittedAt(evidence.getSubmittedAt())
                .challengeId(evidence.getChallengeMember().getChallenge().getId())
                .challengeName(evidence.getChallengeMember().getChallenge().getName())
                .build();
    }
} 