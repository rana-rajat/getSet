package com.getset.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getset.auth.dto.AuthResponse;
import com.getset.auth.dto.LoginRequest;
import com.getset.auth.dto.RegisterRequest;
import com.getset.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_ShouldReturn201WithAuthResponse() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .role(Role.RENTER)
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .id("123")
                .name("Test User")
                .email("test@example.com")
                .role(Role.RENTER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_ShouldReturn200WithAuthResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .id("123")
                .name("Test User")
                .email("test@example.com")
                .role(Role.RENTER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
