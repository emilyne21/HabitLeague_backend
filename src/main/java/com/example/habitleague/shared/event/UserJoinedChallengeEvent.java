package com.example.habitleague.shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserJoinedChallengeEvent {
    private final Long userId;
    private final String userEmail;
    private final Long challengeId;
    private final String challengeName;     // nuevo
    private final Long challengeMemberId;
}
