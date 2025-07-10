package com.example.habitleague.user.service;

import com.example.habitleague.user.dto.PatchUserProfileRequest;
import com.example.habitleague.user.dto.UserProfileResponse;
import com.example.habitleague.user.model.User;
import com.example.habitleague.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(User user) {
        log.debug("Obteniendo perfil para usuario: {}", user.getEmail());
        return UserProfileResponse.fromUser(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, PatchUserProfileRequest request) {
        log.info("Actualizando perfil para usuario: {}", user.getEmail());
        
        boolean hasChanges = false;

        // Actualizar firstName si está presente
        if (request.getFirstName() != null) {
            String trimmedFirstName = request.getFirstName().trim();
            if (!Objects.equals(user.getFirstName(), trimmedFirstName)) {
                log.debug("Actualizando firstName de '{}' a '{}'", user.getFirstName(), trimmedFirstName);
                user.setFirstName(trimmedFirstName);
                hasChanges = true;
            }
        }

        // Actualizar lastName si está presente
        if (request.getLastName() != null) {
            String trimmedLastName = request.getLastName().trim();
            if (!Objects.equals(user.getLastName(), trimmedLastName)) {
                log.debug("Actualizando lastName de '{}' a '{}'", user.getLastName(), trimmedLastName);
                user.setLastName(trimmedLastName);
                hasChanges = true;
            }
        }

        // Actualizar bio si está presente
        if (request.getBio() != null) {
            String trimmedBio = request.getBio().trim();
            if (!Objects.equals(user.getBio(), trimmedBio)) {
                log.debug("Actualizando bio del usuario: {}", user.getEmail());
                user.setBio(trimmedBio);
                hasChanges = true;
            }
        }

        // Actualizar profilePhotoUrl si está presente
        if (request.getProfilePhotoUrl() != null) {
            String trimmedUrl = request.getProfilePhotoUrl().trim();
            if (!Objects.equals(user.getProfilePhotoUrl(), trimmedUrl)) {
                log.debug("Actualizando profilePhotoUrl para usuario: {}", user.getEmail());
                user.setProfilePhotoUrl(trimmedUrl);
                hasChanges = true;
            }
        }

        // Actualizar avatarId si está presente
        if (request.getAvatarId() != null) {
            if (!Objects.equals(user.getAvatarId(), request.getAvatarId())) {
                log.debug("Actualizando avatarId de '{}' a '{}' para usuario: {}", 
                    user.getAvatarId(), request.getAvatarId(), user.getEmail());
                user.setAvatarId(request.getAvatarId());
                hasChanges = true;
            }
        }

        // Solo guardar si hay cambios
        if (hasChanges) {
            userRepository.save(user);
            log.info("Perfil actualizado exitosamente para usuario: {}", user.getEmail());
        } else {
            log.debug("No hay cambios para actualizar en el perfil del usuario: {}", user.getEmail());
        }

        return UserProfileResponse.fromUser(user);
    }
} 