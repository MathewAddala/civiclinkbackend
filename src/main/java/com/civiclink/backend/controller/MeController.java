package com.civiclink.backend.controller;

import com.civiclink.backend.dto.UserDto;
import com.civiclink.backend.security.JwtService;
import com.civiclink.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final JwtService jwtService;
    private final AuthService authService;

    public MeController(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> profile(@RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(authService.me(email));
    }
}
