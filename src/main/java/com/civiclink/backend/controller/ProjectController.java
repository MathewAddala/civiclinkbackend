package com.civiclink.backend.controller;

import com.civiclink.backend.dto.ProjectCreateRequest;
import com.civiclink.backend.dto.ProjectSupportRequest;
import com.civiclink.backend.entity.Project;
import com.civiclink.backend.security.JwtService;
import com.civiclink.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final JwtService jwtService;

    public ProjectController(ProjectService projectService, JwtService jwtService) {
        this.projectService = projectService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> list() {
        return ResponseEntity.ok(projectService.list());
    }

    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody ProjectCreateRequest request,
                                          @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(projectService.create(request, email));
    }

    @PutMapping("/{id}/support")
    public ResponseEntity<Project> support(@PathVariable Long id,
                                           @RequestBody ProjectSupportRequest request,
                                           @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(projectService.support(id, request, email));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Project> approve(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(projectService.approve(id, email));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Project> reject(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(projectService.reject(id, email));
    }
}
