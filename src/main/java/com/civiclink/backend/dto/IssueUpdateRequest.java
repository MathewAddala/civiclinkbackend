package com.civiclink.backend.dto;

public record IssueUpdateRequest(
        String status,
        String assignedTo,
        String priority
) {}
