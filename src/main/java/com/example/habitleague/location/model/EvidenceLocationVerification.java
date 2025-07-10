package com.example.habitleague.location.model;

import com.example.habitleague.evidence.model.Evidence;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidence_location_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceLocationVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double currentLatitude; // Ubicación actual del usuario

    @Column(nullable = false)
    private Double currentLongitude; // Ubicación actual del usuario

    @Column(nullable = false)
    private Double registeredLatitude; // Ubicación registrada del challenge

    @Column(nullable = false)
    private Double registeredLongitude; // Ubicación registrada del challenge

    @Column(nullable = false)
    private Double distanceFromRegistered; // Distancia en metros entre ubicaciones

    @Column(nullable = false)
    private Boolean isWithinTolerance; // Si está dentro del radio de tolerancia

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationVerificationResult result;

    @Column(nullable = false)
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id", nullable = false)
    private Evidence evidence;

    @PrePersist
    protected void onCreate() {
        if (verifiedAt == null) {
            verifiedAt = LocalDateTime.now();
        }
    }
} 