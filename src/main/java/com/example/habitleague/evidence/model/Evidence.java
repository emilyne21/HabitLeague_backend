package com.example.habitleague.evidence.model;

import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.location.model.EvidenceLocationVerification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evidences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evidence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean aiValidated;

    @Column(nullable = false)
    private Double latitude; 

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Boolean locationValid;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_member_id", nullable = false)
    private ChallengeMember challengeMember;

    @OneToMany(mappedBy = "evidence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EvidenceLocationVerification> locationVerifications;
} 