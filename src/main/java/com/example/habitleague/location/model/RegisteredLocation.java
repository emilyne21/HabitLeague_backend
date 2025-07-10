package com.example.habitleague.location.model;

import com.example.habitleague.challenge.model.ChallengeMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registered_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private String address; // Dirección legible obtenida del servicio de maps

    @Column
    private String locationName; // Nombre del lugar (ej: "Gym Central", "Parque Nacional")

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Column
    private Double toleranceRadius; // Radio de tolerancia en metros para verificar evidencias

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_member_id", nullable = false)
    private ChallengeMember challengeMember;

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }
} 