package com.getset.auth;

import com.getset.auth.dto.AuthResponse;
import com.getset.auth.dto.LoginRequest;
import com.getset.auth.dto.RegisterRequest;
import com.getset.auth.dto.UserResponse;
import com.getset.security.JwtService;
import com.getset.user.UserDocument;
import com.getset.user.UserRepository;
import com.getset.util.AuditLogger;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogger auditLogger;

    @Retry(name = "userRepository")
    @CircuitBreaker(name = "userRepository", fallbackMethod = "registerFallback")
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing user registration");

        if (userRepository.existsByEmail(request.getEmail())) {
            auditLogger.logRegistrationAttempt(request.getEmail(), request.getRole().toString(), false,
                    "Email already exists");
            log.warn("Registration failed: email already exists");
            throw new IllegalArgumentException("Email already exists");
        }

        var user = UserDocument.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .build();

        userRepository.save(user);
        auditLogger.logRegistrationAttempt(request.getEmail(), request.getRole().toString(), true,
                "Registration successful");
        log.info("User registered successfully");

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse registerFallback(RegisterRequest request, Exception ex) {
        log.error("Registration fallback triggered due to: {}", ex.getMessage());
        throw new RuntimeException("Service temporarily unavailable, please try again later", ex);
    }

    @Retry(name = "userRepository")
    @CircuitBreaker(name = "userRepository", fallbackMethod = "loginFallback")
    public AuthResponse login(LoginRequest request) {
        log.info("Processing user login");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
            auditLogger.logAuthenticationAttempt(request.getEmail(), true, "Authentication successful");
        } catch (AuthenticationException ex) {
            auditLogger.logAuthenticationAttempt(request.getEmail(), false, "Invalid credentials");
            log.warn("Authentication failed for email: {}", request.getEmail());
            throw ex;
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditLogger.logAuthenticationAttempt(request.getEmail(), false, "User not found");
                    log.error("User not found after authentication");
                    return new UsernameNotFoundException("User not found");
                });

        var jwtToken = jwtService.generateToken(user);
        log.info("User login successful");

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse loginFallback(LoginRequest request, Exception ex) {
        log.error("Login fallback triggered", ex);
        throw new RuntimeException("Service temporarily unavailable, please try again later", ex);
    }

    @Retry(name = "userRepository")
    @CircuitBreaker(name = "userRepository", fallbackMethod = "getCurrentUserFallback")
    public UserResponse getCurrentUser(String email) {
        log.info("Fetching current user");

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    auditLogger.logAuthorizationFailure(email, "USER", "FETCH");
                    log.error("User not found: {}", email);
                    return new UsernameNotFoundException("User not found");
                });

        auditLogger.logResourceAccess(user.getId(), "USER", user.getId(), "FETCH");
        log.info("Current user fetched successfully");

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserResponse getCurrentUserFallback(String email, Exception ex) {
        log.error("Get current user fallback triggered", ex);
        throw new RuntimeException("Service temporarily unavailable, please try again later", ex);
    }
}