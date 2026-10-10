package com.busbooking.controller;

import com.busbooking.dto.AuthResponse;
import com.busbooking.dto.LoginRequest;
import com.busbooking.dto.RegisterRequest;
import com.busbooking.exception.GlobalExceptionHandler;
import com.busbooking.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    @InjectMocks AuthController controller;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_validRequest_returns201WithToken() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("jwt", "Asha", "asha@test.com", "USER"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Asha\",\"email\":\"asha@test.com\",\"password\":\"secret1\",\"phone\":\"9999999999\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_invalidEmailAndShortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Asha\",\"email\":\"not-an-email\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void register_duplicateEmail_returns400WithMessage() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email is already registered"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Asha\",\"email\":\"asha@test.com\",\"password\":\"secret1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt", "Asha", "asha@test.com", "USER"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"asha@test.com\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("asha@test.com"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"asha@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
