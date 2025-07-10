package com.example.habitleague.challenge.repository;

import com.example.habitleague.challenge.model.PrizeDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeDistributionRepository extends JpaRepository<PrizeDistribution, Long> {
    
    List<PrizeDistribution> findByChallengeId(Long challengeId);
    
    List<PrizeDistribution> findByChallengeMemberId(Long challengeMemberId);
    
    List<PrizeDistribution> findByPaidFalse();
} 