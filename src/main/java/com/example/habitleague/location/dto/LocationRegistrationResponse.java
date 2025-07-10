package com.example.habitleague.location.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LocationRegistrationResponse {
    
    private Long registrationId;
    private Double latitude;
    private Double longitude;
    private String address;
    private String locationName;
    private LocalDateTime registeredAt;
    private Double toleranceRadius;
    private String challengeName;
    private Long challengeId;
} 