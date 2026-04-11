package com.civiclink.backend.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StartVotingEventRequest(
        @NotNull @Min(0) Long budgetTokensPerCitizen,
        @NotNull @Min(0) Long projectTokensPerCitizen
) {}
