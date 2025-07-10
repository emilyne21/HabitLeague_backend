package com.example.habitleague.auth.controller;

import com.example.habitleague.auth.dto.AuthenticationRequest;
import com.example.habitleague.auth.dto.RegisterRequest;
import com.example.habitleague.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register")
    public ResponseEntity<String> register(
        @RequestBody @Valid RegisterRequest request) {
        String token = authService.register(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid AuthenticationRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }
} 