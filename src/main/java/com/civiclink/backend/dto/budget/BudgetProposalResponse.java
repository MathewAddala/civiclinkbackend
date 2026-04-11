package com.civiclink.backend.dto.budget;

import java.time.Instant;

public record BudgetProposalResponse(
        Long id,
        Integer transport,
        Integer healthcare,
        Integer environment,
        Integer sanitation,
        Integer education,
        Long tokensSpent,
        Long transportTokens,
        Long healthcareTokens,
        Long environmentTokens,
        Long sanitationTokens,
        Long educationTokens,
        String status,
        Instant createdAt,
        String submittedBy
) {}
