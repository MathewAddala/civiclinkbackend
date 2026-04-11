package com.civiclink.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record IssueCreateRequest(
        @NotBlank String title,
        String description,
        @NotBlank String location,
        Double lat,
        Double lng,
        String priority
) {}
