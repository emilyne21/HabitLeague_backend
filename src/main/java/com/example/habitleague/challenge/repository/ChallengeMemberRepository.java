package com.example.habitleague.challenge.repository;

import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeMemberRepository extends JpaRepository<ChallengeMember, Long> {
    boolean existsByUserAndChallenge(User user, Challenge challenge);
    Optional<ChallengeMember> findByUserAndChallenge(User user, Challenge challenge);
    
    @Query("SELECT cm FROM ChallengeMember cm JOIN FETCH cm.user WHERE cm.challenge = :challenge")
    List<ChallengeMember> findByChallenge(Challenge challenge);
    
    @Query("SELECT cm FROM ChallengeMember cm WHERE cm.user.id = :userId AND cm.challenge.id = :challengeId")
    Optional<ChallengeMember> findByUserIdAndChallengeId(@Param("userId") Long userId, @Param("challengeId") Long challengeId);
    
    @Query("SELECT cm FROM ChallengeMember cm " +
           "JOIN FETCH cm.challenge c " +
           "JOIN FETCH c.createdBy " +
           "WHERE cm.user = :user " +
           "ORDER BY cm.joinedAt DESC")
    List<ChallengeMember> findByUserWithChallengeAndCreator(@Param("user") User user);
    
    // Nuevos métodos para el sistema de pricepool
    List<ChallengeMember> findByChallengeAndHasCompletedTrue(Challenge challenge);
    List<ChallengeMember> findByChallengeAndPaymentCompletedTrue(Challenge challenge);
    
    @Query("SELECT COUNT(cm) FROM ChallengeMember cm WHERE cm.challenge = :challenge AND cm.hasCompleted = true")
    int countByChallengeAndHasCompletedTrue(@Param("challenge") Challenge challenge);
} 