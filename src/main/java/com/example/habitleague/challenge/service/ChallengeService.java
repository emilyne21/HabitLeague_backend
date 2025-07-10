package com.example.habitleague.challenge.service;

import com.example.habitleague.challenge.dto.CreateChallengeCompleteRequest;
import com.example.habitleague.challenge.model.Challenge;
import com.example.habitleague.challenge.model.ChallengeCategory;
import com.example.habitleague.challenge.model.ChallengeMember;
import com.example.habitleague.challenge.model.ChallengeStatus;
import com.example.habitleague.challenge.repository.ChallengeMemberRepository;
import com.example.habitleague.challenge.repository.ChallengeRepository;
import com.example.habitleague.shared.event.ChallengeCreatedEvent;
import com.example.habitleague.shared.event.UserJoinedChallengeEvent;
import com.example.habitleague.shared.exception.ChallengeException;
import com.example.habitleague.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository       challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ApplicationEventPublisher publisher;
    private final ChallengeLifecycleService lifecycleService;

    @Transactional
    public Challenge createChallengeWithoutMember(CreateChallengeCompleteRequest request, User user) {
        
        // Validar duración nueva (en días)
        if (request.getDurationDays() != null && !isValidDurationDays(request.getDurationDays())) {
            throw new IllegalArgumentException("La duración debe ser entre 21 y 365 días");
        }

        Challenge challenge = Challenge.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .rules(request.getRules())
                .durationDays(request.getDurationDays())
                .entryFee(request.getEntryFee())
                .featured(false) 
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ChallengeStatus.CREATED)
                .createdBy(user)
                .build();

        Challenge saved = challengeRepository.save(challenge);
        
        // Publicar evento de creación
        publisher.publishEvent(new ChallengeCreatedEvent(
                saved.getId(),
                user.getEmail(),
                saved.getName()
        ));

        return saved;
    }

    @Transactional
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException("Challenge no encontrado"));
        challengeRepository.delete(challenge);
    }


    @Transactional
    public ChallengeMember joinChallengeWithPaymentAndLocation(Long challengeId, User user, boolean paymentCompleted, boolean locationRegistered) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException("Reto no encontrado"));

        if (challengeMemberRepository.existsByUserAndChallenge(user, challenge)) {
            throw new ChallengeException("Ya estás participando en este reto");
        }

        // Verificar que ambos requisitos estén completados
        if (!paymentCompleted) {
            throw new ChallengeException("Debe completar el pago antes de unirse al reto");
        }

        if (!locationRegistered) {
            throw new ChallengeException("Debe registrar su ubicación antes de unirse al reto");
        }

        ChallengeMember member = ChallengeMember.builder()
                .challenge(challenge)
                .user(user)
                .joinedAt(LocalDate.now())
                .paymentCompleted(paymentCompleted)
                .locationRegistered(locationRegistered)
                .build();

        member = challengeMemberRepository.save(member);

        // Inicializar/actualizar pricepool cuando alguien se une
        lifecycleService.initializePricepool(challenge);

        publisher.publishEvent(new UserJoinedChallengeEvent(
                user.getId(),
                user.getEmail(),
                challenge.getId(),
                challenge.getName(),
                member.getId()
        ));

        return member;
    }

    @Transactional
    public ChallengeMember createChallengeMemberPartial(Long challengeId, User user, boolean paymentCompleted, boolean locationRegistered) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException("Reto no encontrado"));

        if (challengeMemberRepository.existsByUserAndChallenge(user, challenge)) {
            throw new ChallengeException("Ya estás participando en este reto");
        }

        // Para la creación de miembros no validamos que ambos estén completados
        ChallengeMember member = ChallengeMember.builder()
                .challenge(challenge)
                .user(user)
                .joinedAt(LocalDate.now())
                .paymentCompleted(paymentCompleted)
                .locationRegistered(locationRegistered)
                .build();

        member = challengeMemberRepository.save(member);

        // Solo publicar evento si ambos requisitos están completados
        if (paymentCompleted && locationRegistered) {
            // Inicializar/actualizar pricepool cuando alguien se une completamente
            lifecycleService.initializePricepool(challenge);
            
            publisher.publishEvent(new UserJoinedChallengeEvent(
                    user.getId(),
                    user.getEmail(),
                    challenge.getId(),
                    challenge.getName(),
                    member.getId()
            ));
        }

        return member;
    }

    @Transactional
    public ChallengeMember updateChallengeMemberLocation(ChallengeMember member, boolean locationRegistered) {
        member.setLocationRegistered(locationRegistered);
        member = challengeMemberRepository.save(member);

        // Publicar evento si ahora ambos requisitos están completados
        if (member.getPaymentCompleted() && locationRegistered) {
            // Inicializar/actualizar pricepool cuando el miembro completa todos los requisitos
            lifecycleService.initializePricepool(member.getChallenge());
            
            publisher.publishEvent(new UserJoinedChallengeEvent(
                    member.getUser().getId(),
                    member.getUser().getEmail(),
                    member.getChallenge().getId(),
                    member.getChallenge().getName(),
                    member.getId()
            ));
        }

        return member;
    }

    @Transactional(readOnly = true)
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ChallengeMember> getParticipants(Long challengeId, User user) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException("Reto no encontrado"));

        List<ChallengeMember> members = challengeMemberRepository.findByChallenge(challenge);
        members.forEach(m -> m.getUser().getFirstName());
        return members;
    }

    // Nuevos métodos
    @Transactional(readOnly = true)
    public List<Challenge> getChallengesByCategory(ChallengeCategory category) {
        return challengeRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Challenge> getFeaturedChallenges() {
        return challengeRepository.findFeaturedChallenges();
    }

    @Transactional(readOnly = true)
    public List<Challenge> getPopularChallenges(int limit) {
        return challengeRepository.findPopularChallenges(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public Challenge getChallengeById(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new ChallengeException("Reto no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<ChallengeMember> getUserChallenges(User user) {
        return challengeMemberRepository.findByUserWithChallengeAndCreator(user);
    }

    /**
     * Obtiene challenges populares con información de ubicación del creador
     */
    @Transactional(readOnly = true)
    public List<Challenge> getPopularChallengesWithLocation(int limit) {
        return challengeRepository.findPopularChallenges(PageRequest.of(0, limit));
    }

    /**
     * Obtiene challenges por categoría con información de ubicación del creador
     */
    @Transactional(readOnly = true)
    public List<Challenge> getChallengesByCategoryWithLocation(ChallengeCategory category) {
        return challengeRepository.findByCategory(category);
    }

    /**
     * Obtiene challenges destacados con información de ubicación del creador
     */
    @Transactional(readOnly = true)
    public List<Challenge> getFeaturedChallengesWithLocation() {
        return challengeRepository.findFeaturedChallenges();
    }

    private boolean isValidDurationDays(Integer durationDays) {
        return durationDays >= 21 && durationDays <= 365;
    }
}

