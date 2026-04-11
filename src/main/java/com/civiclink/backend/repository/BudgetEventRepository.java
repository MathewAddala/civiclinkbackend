package com.civiclink.backend.repository;

import com.civiclink.backend.entity.BudgetEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetEventRepository extends JpaRepository<BudgetEvent, Long> {
    BudgetEvent findTopByOrderByStartedAtDesc();
    BudgetEvent findTopByActiveTrueOrderByStartedAtDesc();
}
