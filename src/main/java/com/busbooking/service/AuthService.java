package com.busbooking.service;

import com.busbooking.dto.AuthResponse;
import com.busbooking.dto.LoginRequest;
import com.busbooking.dto.RegisterRequest;
import com.busbooking.entity.Role;
import com.busbooking.entity.User;
import com.busbooking.repository.UserRepository;
import com.busbooking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = userRepository.save(User.builder()
                .name(req.name().trim())
                .email(email)
                .password(passwordEncoder.encode(req.password()))
                .phone(req.phone())
                .role(Role.USER)
                .build());
        return new AuthResponse(jwtUtil.generateToken(user.getEmail()),
                user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password()));
        User user = userRepository.findByEmail(email).orElseThrow();
        return new AuthResponse(jwtUtil.generateToken(email),
                user.getName(), user.getEmail(), user.getRole().name());
    }
}
