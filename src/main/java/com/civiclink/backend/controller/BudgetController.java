package com.civiclink.backend.controller;

import com.civiclink.backend.dto.budget.BudgetProposalRequest;
import com.civiclink.backend.dto.budget.BudgetProposalResponse;
import com.civiclink.backend.security.JwtService;
import com.civiclink.backend.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {
    private final BudgetService budgetService;
    private final JwtService jwtService;

    public BudgetController(BudgetService budgetService, JwtService jwtService) {
        this.budgetService = budgetService;
        this.jwtService = jwtService;
    }

    @GetMapping("/current")
    public ResponseEntity<BudgetProposalResponse> current() {
        return ResponseEntity.ok(budgetService.getCurrentAllocation());
    }

    @GetMapping("/proposals")
    public ResponseEntity<List<BudgetProposalResponse>> proposals() {
        return ResponseEntity.ok(budgetService.listRecent());
    }

    @PostMapping("/proposals")
    public ResponseEntity<BudgetProposalResponse> submit(@Valid @RequestBody BudgetProposalRequest request,
                                                         @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(budgetService.submit(request, email));
    }

    @PutMapping("/proposals/{id}/approve")
    public ResponseEntity<BudgetProposalResponse> approve(@PathVariable Long id,
                                                          @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(budgetService.approve(id, email));
    }

    @PutMapping("/approve-average")
    public ResponseEntity<BudgetProposalResponse> approveAverage(@RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(budgetService.approveByAverage(email));
    }

    @PutMapping("/current")
    public ResponseEntity<BudgetProposalResponse> overrideCurrent(@Valid @RequestBody BudgetProposalRequest request,
                                                                  @RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        return ResponseEntity.ok(budgetService.overrideCurrent(request, email));
    }
}
