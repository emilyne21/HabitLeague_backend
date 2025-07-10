package com.example.habitleague.user.controller;

import com.example.habitleague.user.dto.PatchUserProfileRequest;
import com.example.habitleague.user.dto.UserProfileResponse;
import com.example.habitleague.user.model.User;
import com.example.habitleague.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        log.debug("Solicitud GET /api/user/profile para usuario: {}", user.getEmail());
        UserProfileResponse response = userService.getProfile(user);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    @Transactional
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody @Valid PatchUserProfileRequest request,
            @AuthenticationPrincipal User user) {
        log.info("Solicitud PATCH /api/user/profile para usuario: {}", user.getEmail());
        UserProfileResponse response = userService.updateProfile(user, request);
        return ResponseEntity.ok(response);
    }
} 