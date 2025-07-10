package com.example.habitleague.challenge.dto;

import com.example.habitleague.challenge.model.ChallengeCategory;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DiscoverResponse {
    private List<ChallengeSummaryResponse> featured;
    private List<ChallengeSummaryResponse> popular;
    private Map<ChallengeCategory, List<ChallengeSummaryResponse>> byCategory;
} 