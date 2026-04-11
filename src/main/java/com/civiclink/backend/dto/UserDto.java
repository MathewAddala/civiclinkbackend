package com.civiclink.backend.dto;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        Long budgetTokens,
        Long projectTokens
) {}
