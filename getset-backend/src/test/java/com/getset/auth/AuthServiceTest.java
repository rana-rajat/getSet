package com.getset.auth;

import com.getset.auth.dto.AuthResponse;
import com.getset.auth.dto.LoginRequest;
import com.getset.auth.dto.RegisterRequest;
import com.getset.auth.dto.RefreshTokenRequest;
import com.getset.auth.dto.UserResponse;
import com.getset.exception.UnauthorizedException;
import com.getset.security.JwtService;
import com.getset.user.Role;
import com.getset.user.UserDocument;
import com.getset.user.UserRepository;
import com.getset.util.AuditLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private AuthService authService;

    private UserDocument testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private final String testEmail = "test@example.com";
    private final String testToken = "test.jwt.token";
    private final String testRefreshToken = "test.refresh.token";

    @BeforeEach
    void setUp() {
        testUser = UserDocument.builder()
                .id("u1")
                .name("Test User")
                .email(testEmail)
                .password("encoded_password")
                .role(Role.RENTER)
                .phone("1234567890")
                .createdAt(Instant.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .name("Test User")
                .email(testEmail)
                .password("password")
                .role(Role.RENTER)
                .build();

        loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password("password")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(UserDocument.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(UserDocument.class))).thenReturn(testToken);
        when(jwtService.generateRefreshToken(any(UserDocument.class))).thenReturn(testRefreshToken);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(testToken, response.getAccessToken());
        assertEquals(testRefreshToken, response.getRefreshToken());
        assertEquals(testEmail, response.getEmail());
        
        verify(userRepository).save(any(UserDocument.class));
        verify(auditLogger).logRegistrationAttempt(eq(testEmail), eq("RENTER"), eq(true), anyString());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(UserDocument.class));
        verify(auditLogger).logRegistrationAttempt(eq(testEmail), eq("RENTER"), eq(false), anyString());
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(testToken);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals(testToken, response.getAccessToken());
        assertEquals(testEmail, response.getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(auditLogger).logAuthenticationAttempt(eq(testEmail), eq(true), anyString());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(auditLogger).logAuthenticationAttempt(eq(testEmail), eq(false), anyString());
    }

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(testRefreshToken);

        when(jwtService.extractUsername(testRefreshToken)).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(testRefreshToken, testUser)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(testToken);

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals(testToken, response.getAccessToken());
        assertEquals(testRefreshToken, response.getRefreshToken());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsUnauthorized() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(testRefreshToken);

        when(jwtService.extractUsername(testRefreshToken)).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(testRefreshToken, testUser)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
    }

    @Test
    void getCurrentUser_Success() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        UserResponse response = authService.getCurrentUser(testEmail);

        assertNotNull(response);
        assertEquals(testEmail, response.getEmail());
        assertEquals(testUser.getId(), response.getId());
        verify(auditLogger).logResourceAccess(eq(testUser.getId()), anyString(), eq(testUser.getId()), eq("FETCH"));
    }

    @Test
    void getCurrentUser_NotFound_ThrowsException() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.getCurrentUser(testEmail));
        verify(auditLogger).logAuthorizationFailure(eq(testEmail), anyString(), eq("FETCH"));
    }
}
