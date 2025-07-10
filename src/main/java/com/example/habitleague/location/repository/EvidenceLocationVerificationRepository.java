package com.example.habitleague.location.repository;

import com.example.habitleague.evidence.model.Evidence;
import com.example.habitleague.location.model.EvidenceLocationVerification;
import com.example.habitleague.location.model.LocationVerificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EvidenceLocationVerificationRepository extends JpaRepository<EvidenceLocationVerification, Long> {
    
    List<EvidenceLocationVerification> findByEvidence(Evidence evidence);
    
    List<EvidenceLocationVerification> findByResult(LocationVerificationResult result);
    
    @Query("SELECT elv FROM EvidenceLocationVerification elv WHERE elv.evidence.challengeMember.user.id = :userId")
    List<EvidenceLocationVerification> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT elv FROM EvidenceLocationVerification elv WHERE elv.evidence.challengeMember.challenge.id = :challengeId")
    List<EvidenceLocationVerification> findByChallengeId(@Param("challengeId") Long challengeId);
    
    @Query("SELECT elv FROM EvidenceLocationVerification elv WHERE elv.verifiedAt BETWEEN :startDate AND :endDate")
    List<EvidenceLocationVerification> findByVerifiedAtBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(elv) FROM EvidenceLocationVerification elv WHERE elv.result = :result AND elv.evidence.challengeMember.user.id = :userId")
    long countByResultAndUserId(@Param("result") LocationVerificationResult result, @Param("userId") Long userId);
    
    boolean existsByEvidenceAndResult(Evidence evidence, LocationVerificationResult result);
} 