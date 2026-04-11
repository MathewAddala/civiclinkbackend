package com.civiclink.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "budget_events")
public class BudgetEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Long budgetTokensGranted;

    @Column(nullable = false)
    private Long projectTokensGranted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by_user_id")
    private User startedBy;

    @PrePersist
    public void prePersist() {
        if (startedAt == null) startedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Long getBudgetTokensGranted() { return budgetTokensGranted; }
    public void setBudgetTokensGranted(Long budgetTokensGranted) { this.budgetTokensGranted = budgetTokensGranted; }
    public Long getProjectTokensGranted() { return projectTokensGranted; }
    public void setProjectTokensGranted(Long projectTokensGranted) { this.projectTokensGranted = projectTokensGranted; }
    public User getStartedBy() { return startedBy; }
    public void setStartedBy(User startedBy) { this.startedBy = startedBy; }
}
