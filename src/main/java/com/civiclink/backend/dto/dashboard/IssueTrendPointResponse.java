package com.civiclink.backend.dto.dashboard;

public record IssueTrendPointResponse(
        String name,
        long reported,
        long resolved
) {}
