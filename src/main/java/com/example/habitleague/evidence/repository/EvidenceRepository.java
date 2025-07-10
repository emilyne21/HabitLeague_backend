package com.example.habitleague.evidence.repository;

import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.evidence.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    boolean existsByChallengeMemberAndSubmittedAtBetween(
        ChallengeMember challengeMember, 
        LocalDateTime start, 
        LocalDateTime end
    );
    List<Evidence> findByChallengeMember_User_Id(Long userId);
}