package com.civiclink.backend.controller;

import com.civiclink.backend.dto.dashboard.ActivityItemResponse;
import com.civiclink.backend.dto.dashboard.DashboardSummaryResponse;
import com.civiclink.backend.dto.dashboard.IssueTrendPointResponse;
import com.civiclink.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(dashboardService.summary());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityItemResponse>> activity() {
        return ResponseEntity.ok(dashboardService.activity());
    }

    @GetMapping("/issue-trends")
    public ResponseEntity<List<IssueTrendPointResponse>> issueTrends() {
        return ResponseEntity.ok(dashboardService.issueTrends());
    }
}
