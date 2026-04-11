package com.civiclink.backend.dto.dashboard;

import java.time.Instant;

public record ActivityItemResponse(
        String type,
        String message,
        Instant createdAt
) {}
