package com.civiclink.backend.repository;

import com.civiclink.backend.entity.BudgetProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetProposalRepository extends JpaRepository<BudgetProposal, Long> {
    List<BudgetProposal> findTop20ByOrderByCreatedAtDesc();
    BudgetProposal findTopByStatusOrderByCreatedAtDesc(String status);
    List<BudgetProposal> findByStatusOrderByCreatedAtDesc(String status);
}
