package com.example.habitleague.location.repository;

import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.location.model.RegisteredLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredLocationRepository extends JpaRepository<RegisteredLocation, Long> {
    
    Optional<RegisteredLocation> findByChallengeMember(ChallengeMember challengeMember);
    
    @Query("SELECT rl FROM RegisteredLocation rl WHERE rl.challengeMember.user.id = :userId")
    List<RegisteredLocation> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT rl FROM RegisteredLocation rl WHERE rl.challengeMember.challenge.id = :challengeId")
    List<RegisteredLocation> findByChallengeId(@Param("challengeId") Long challengeId);
    
    @Query("SELECT rl FROM RegisteredLocation rl WHERE rl.challengeMember.user.id = :userId AND rl.challengeMember.challenge.id = :challengeId")
    Optional<RegisteredLocation> findByUserIdAndChallengeId(
        @Param("userId") Long userId, 
        @Param("challengeId") Long challengeId
    );
    
    boolean existsByChallengeMember(ChallengeMember challengeMember);
} 