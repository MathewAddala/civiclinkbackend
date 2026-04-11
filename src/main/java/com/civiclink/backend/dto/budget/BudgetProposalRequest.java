package com.civiclink.backend.dto.budget;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BudgetProposalRequest(
        @NotNull @Min(0) Long transportTokens,
        @NotNull @Min(0) Long healthcareTokens,
        @NotNull @Min(0) Long environmentTokens,
        @NotNull @Min(0) Long sanitationTokens,
        @NotNull @Min(0) Long educationTokens
) {}
