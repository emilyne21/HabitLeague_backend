package com.example.habitleague.achievement.model;

import com.example.habitleague.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un logro desbloqueado por un usuario
 */
@Entity
@Table(name = "user_achievements", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "achievement_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime unlockedAt;

    @Column(name = "challenge_id")
    private Long challengeId; // Referencia al reto relacionado (opcional)

    @Column(length = 255)
    private String contextInfo; // Información adicional sobre el contexto del logro
} 