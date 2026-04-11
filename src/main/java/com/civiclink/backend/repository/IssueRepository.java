package com.civiclink.backend.repository;

import com.civiclink.backend.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    long countByStatus(String status);
    List<Issue> findTop20ByOrderByCreatedAtDesc();
    List<Issue> findBySubmittedBy_EmailOrderByCreatedAtDesc(String email);
    List<Issue> findAllByOrderByCreatedAtDesc();
}
