package com.getset.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    // Minimum 32 chars, base64 encoded for HS256
    // "test-secret-key-that-is-very-long-and-secure" -> base64 ->
    // dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtdmVyeS1sb25nLWFuZC1zZWN1cmU=
    private final String secretKey = "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtdmVyeS1sb25nLWFuZC1zZWN1cmU=";
    private final long jwtExpiration = 1000 * 60 * 60; // 1 hour
    private final long refreshExpiration = 1000 * 60 * 60 * 24 * 7; // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

        userDetails = new User("testuser@example.com", "password", new ArrayList<>());
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        String expectedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), expectedUsername);
    }

    @Test
    void generateRefreshToken_ShouldReturnValidJwtToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        String expectedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), expectedUsername);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithWrongUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = new User("other@example.com", "password", new ArrayList<>());
        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Set very short expiration for testing
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 millisecond

        String token = jwtService.generateToken(userDetails);

        // Wait for token to expire
        Thread.sleep(10);

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(token, userDetails);
        });
    }
}
