package com.example.habitleague.challenge.repository;

import com.example.habitleague.challenge.model.DailyEvidenceCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyEvidenceCheckRepository extends JpaRepository<DailyEvidenceCheck, Long> {
    
    boolean existsByChallengeIdAndCheckDate(Long challengeId, LocalDate checkDate);
    
    List<DailyEvidenceCheck> findByChallengeIdOrderByCheckDateDesc(Long challengeId);
} 