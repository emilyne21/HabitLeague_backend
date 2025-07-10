package com.example.habitleague.challenge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ChallengeParticipantResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDate joinedAt;
} 