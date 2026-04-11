package com.civiclink.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull Double goalAmount,
        @NotBlank String sector
) {}
