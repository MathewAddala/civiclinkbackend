package com.civiclink.backend.controller;

import com.civiclink.backend.dto.admin.StartVotingEventRequest;
import com.civiclink.backend.entity.BudgetEvent;
import com.civiclink.backend.entity.User;
import com.civiclink.backend.repository.UserRepository;
import com.civiclink.backend.security.JwtService;
import com.civiclink.backend.service.admin.VotingEventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final JwtService jwtService;
    private final VotingEventService votingEventService;
    private final UserRepository userRepository;

    public AdminController(JwtService jwtService, VotingEventService votingEventService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.votingEventService = votingEventService;
        this.userRepository = userRepository;
    }

    @GetMapping("/voting-event")
    public ResponseEntity<Map<String, Object>> status(@RequestHeader("Authorization") String authorization) {
        jwtService.extractEmail(authorization.replace("Bearer ", ""));
        BudgetEvent active = votingEventService.active();
        // LinkedHashMap: Map.of rejects null values (breaks clients when no active event).
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("active", active != null && active.isActive());
        body.put("startedAt", active != null ? active.getStartedAt() : null);
        body.put("budgetTokensGranted", active != null ? active.getBudgetTokensGranted() : 0);
        body.put("projectTokensGranted", active != null ? active.getProjectTokensGranted() : 0);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/voting-event/start")
    public ResponseEntity<Map<String, Object>> start(@Valid @RequestBody StartVotingEventRequest request,
                                                     @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        BudgetEvent event = votingEventService.start(request, email);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("active", event.isActive());
        body.put("startedAt", event.getStartedAt());
        body.put("budgetTokensGranted", event.getBudgetTokensGranted());
        body.put("projectTokensGranted", event.getProjectTokensGranted());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/voting-event/stop")
    public ResponseEntity<Map<String, Object>> stop(@RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        BudgetEvent event = votingEventService.stop(email);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("active", event.isActive());
        body.put("startedAt", event.getStartedAt());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/users")
    public ResponseEntity<?> users(@RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(actor.getRole())) {
            throw new IllegalArgumentException("Only admin can view users.");
        }
        return ResponseEntity.ok(userRepository.findAll().stream().map(user -> Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        )).toList());
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<?> promote(@PathVariable Long id, @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(actor.getRole())) {
            throw new IllegalArgumentException("Only admin can promote users.");
        }
        User target = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Target user not found."));
        target.setRole("admin");
        userRepository.save(target);
        return ResponseEntity.ok(Map.of(
                "id", target.getId(),
                "name", target.getName(),
                "email", target.getEmail(),
                "role", target.getRole()
        ));
    }
}
