package com.civiclink.backend.dto;

public record AuthResponse(
        UserDto user,
        String token
) {}
