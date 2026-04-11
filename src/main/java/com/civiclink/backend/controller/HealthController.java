package com.civiclink.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {
    @Value("${spring.application.name:civiclink-backend}")
    private String appName;

    @Value("${app.build.tag:unknown}")
    private String buildTag;

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", appName,
                "buildTag", buildTag,
                "time", Instant.now().toString()
        ));
    }
}
