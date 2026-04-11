package com.civiclink.backend.dto.dashboard;

public record DashboardSummaryResponse(
        long activeCitizens,
        long issuesReported,
        long issuesResolved,
        long projectsSupported,
        long pendingBudgetProposals,
        String civicScore
) {}
