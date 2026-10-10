package com.busbooking.service;

import com.busbooking.dto.AuthResponse;
import com.busbooking.dto.LoginRequest;
import com.busbooking.dto.RegisterRequest;
import com.busbooking.entity.Role;
import com.busbooking.entity.User;
import com.busbooking.repository.UserRepository;
import com.busbooking.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void register_savesUserWithHashedPasswordAndNormalisedEmail() {
        when(userRepository.existsByEmail("asha@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("asha@test.com")).thenReturn("jwt-token");

        AuthResponse res = authService.register(new RegisterRequest(" Asha ", " ASHA@Test.com ", "secret1", "9999999999"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("asha@test.com");
        assertThat(saved.getName()).isEqualTo("Asha");
        assertThat(saved.getPassword()).isEqualTo("HASHED");
        assertThat(saved.getRole()).isEqualTo(Role.USER);

        assertThat(res.token()).isEqualTo("jwt-token");
        assertThat(res.role()).isEqualTo("USER");
    }

    @Test
    void register_duplicateEmail_throwsAndSavesNothing() {
        when(userRepository.existsByEmail("asha@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Asha", "asha@test.com", "secret1", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = User.builder().id(1L).name("Asha").email("asha@test.com").password("HASHED").role(Role.ADMIN).build();
        when(userRepository.findByEmail("asha@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("asha@test.com")).thenReturn("jwt-token");

        AuthResponse res = authService.login(new LoginRequest("Asha@test.com", "secret1"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(res.token()).isEqualTo("jwt-token");
        assertThat(res.name()).isEqualTo("Asha");
        assertThat(res.role()).isEqualTo("ADMIN");
    }

    @Test
    void login_wrongPassword_propagatesBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("asha@test.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtUtil, never()).generateToken(any());
    }
}
