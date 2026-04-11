package com.civiclink.backend.controller;

import com.civiclink.backend.dto.AuthResponse;
import com.civiclink.backend.dto.LoginRequest;
import com.civiclink.backend.dto.RegisterRequest;
import com.civiclink.backend.dto.UserDto;
import com.civiclink.backend.security.JwtService;
import com.civiclink.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return ResponseEntity.ok(authService.me(email));
    }
}
