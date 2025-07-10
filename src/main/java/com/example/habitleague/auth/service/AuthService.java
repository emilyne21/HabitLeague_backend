package com.example.habitleague.auth.service;

import com.example.habitleague.auth.dto.AuthenticationRequest;
import com.example.habitleague.auth.dto.RegisterRequest;
import com.example.habitleague.shared.exception.UserAlreadyExistsException;
import com.example.habitleague.user.model.User;
import com.example.habitleague.user.model.UserRole;
import com.example.habitleague.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {
        // Verificar si el email ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }

        // Crear nuevo usuario
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBio(request.getBio());
        user.setAvatarId(request.getAvatarId());
        user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);

        // Guardar usuario
        userRepository.save(user);

        // Generar y retornar token JWT
        return jwtService.generateToken(user.getEmail());
    }

    public String login(AuthenticationRequest request) {
        // Buscar usuario por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Generar y retornar token JWT
        return jwtService.generateToken(user.getEmail());
    }
} 