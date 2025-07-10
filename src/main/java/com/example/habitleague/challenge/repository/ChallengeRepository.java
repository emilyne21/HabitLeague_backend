package com.example.habitleague.challenge.repository;

import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    
    @Query("SELECT c FROM Challenge c WHERE c.category = :category")
    List<Challenge> findByCategory(@Param("category") ChallengeCategory category);
    
    @Query("SELECT c FROM Challenge c WHERE c.featured = true")
    List<Challenge> findFeaturedChallenges();
    
    @Query("SELECT c FROM Challenge c ORDER BY SIZE(c.members) DESC")
    List<Challenge> findPopularChallenges(Pageable pageable);
    
    @Query("SELECT c FROM Challenge c LEFT JOIN FETCH c.members WHERE c.id = :id")
    Optional<Challenge> findByIdWithMembers(@Param("id") Long id);
    
    @Query("SELECT c FROM Challenge c LEFT JOIN FETCH c.members LEFT JOIN FETCH c.createdBy")
    List<Challenge> findAllWithMembersAndCreator();
    
    // Nuevo método para el sistema de pricepool
    @Query("SELECT c FROM Challenge c WHERE c.startDate <= :date AND c.endDate >= :date AND c.prizesDistributed = false")
    List<Challenge> findActiveChallengesForDate(@Param("date") LocalDate date);
} 