package com.getset.user.config;

import com.getset.user.domain.UserRepository;
import com.getset.user.domain.UserDocument;
import com.getset.user.domain.Role;
import com.getset.user.security.JwtService;
import com.getset.common.exception.NotFoundException;
import com.getset.common.exception.UnauthorizedException;
import com.getset.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        var user = UserDocument.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .phone(request.getPhone())
                .build();
        userRepository.save(user);
        log.info("User registered: {}", user.getId());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.getRefreshToken());
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!jwtService.isTokenValid(request.getRefreshToken(), user)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(request.getRefreshToken())
                .id(user.getId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole().name())
                .build();
    }

    public UserResponse getCurrentUser(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.builder()
                .id(user.getId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole().name())
                .phone(user.getPhone()).createdAt(user.getCreatedAt())
                .build();
    }

    private AuthResponse buildAuthResponse(UserDocument user) {
        var extraClaims = new HashMap<String, Object>();
        extraClaims.put("role", user.getRole().name());
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(extraClaims, user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .id(user.getId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole().name())
                .build();
    }
}
