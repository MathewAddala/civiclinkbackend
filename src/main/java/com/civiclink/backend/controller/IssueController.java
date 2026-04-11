package com.civiclink.backend.controller;

import com.civiclink.backend.dto.IssueCreateRequest;
import com.civiclink.backend.dto.IssueUpdateRequest;
import com.civiclink.backend.entity.Issue;
import com.civiclink.backend.security.JwtService;
import com.civiclink.backend.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final IssueService issueService;
    private final JwtService jwtService;

    public IssueController(IssueService issueService, JwtService jwtService) {
        this.issueService = issueService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<Issue>> list(@RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(issueService.list(email));
    }

    @PostMapping
    public ResponseEntity<Issue> create(@Valid @RequestBody IssueCreateRequest request, @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(issueService.create(request, email));
    }

    @PostMapping(value = "/with-attachment", consumes = "multipart/form-data")
    public ResponseEntity<Issue> createWithAttachment(@RequestParam("title") String title,
                                                      @RequestParam("location") String location,
                                                      @RequestParam(value = "description", required = false) String description,
                                                      @RequestParam(value = "lat", required = false) Double lat,
                                                      @RequestParam(value = "lng", required = false) Double lng,
                                                      @RequestParam(value = "priority", required = false) String priority,
                                                      @RequestParam("attachment") MultipartFile attachment,
                                                      @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        IssueCreateRequest request = new IssueCreateRequest(title, description, location, lat, lng, priority);
        return ResponseEntity.ok(issueService.create(request, attachment, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Issue> update(@PathVariable Long id,
                                        @RequestBody IssueUpdateRequest request,
                                        @RequestHeader("Authorization") String authorization) {
        // Token is validated by filter; extraction enforces authenticated flow.
        jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(issueService.update(id, request));
    }
}
