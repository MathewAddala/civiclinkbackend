package com.civiclink.backend.service;

import com.civiclink.backend.dto.AuthResponse;
import com.civiclink.backend.dto.LoginRequest;
import com.civiclink.backend.dto.RegisterRequest;
import com.civiclink.backend.dto.UserDto;
import com.civiclink.backend.entity.User;
import com.civiclink.backend.repository.UserRepository;
import com.civiclink.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Account already exists with this email.");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        // Self-registration is citizen-only. Admin role can only be granted by an existing admin.
        user.setRole("citizen");
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getEmail());
        UserDto dto = UserMapper.toDto(saved);

        return new AuthResponse(dto, token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(UserMapper.toDto(user), token);
    }

    public UserDto me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return UserMapper.toDto(user);
    }
}
