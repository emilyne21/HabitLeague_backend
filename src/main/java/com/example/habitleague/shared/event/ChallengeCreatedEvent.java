package com.example.habitleague.shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChallengeCreatedEvent {
    private final Long challengeId;
    private final String creatorEmail;
    private final String challengeName;
}
