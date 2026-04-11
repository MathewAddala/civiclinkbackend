package com.civiclink.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "budget_proposals")
public class BudgetProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer transport;
    private Integer healthcare;
    private Integer environment;
    private Integer sanitation;
    private Integer education;

    private Long tokensSpent;

    private Long transportTokens;
    private Long healthcareTokens;
    private Long environmentTokens;
    private Long sanitationTokens;
    private Long educationTokens;

    private String status;
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedBy;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "pending";
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getTransport() { return transport; }
    public void setTransport(Integer transport) { this.transport = transport; }
    public Integer getHealthcare() { return healthcare; }
    public void setHealthcare(Integer healthcare) { this.healthcare = healthcare; }
    public Integer getEnvironment() { return environment; }
    public void setEnvironment(Integer environment) { this.environment = environment; }
    public Integer getSanitation() { return sanitation; }
    public void setSanitation(Integer sanitation) { this.sanitation = sanitation; }
    public Integer getEducation() { return education; }
    public void setEducation(Integer education) { this.education = education; }
    public Long getTokensSpent() { return tokensSpent; }
    public void setTokensSpent(Long tokensSpent) { this.tokensSpent = tokensSpent; }
    public Long getTransportTokens() { return transportTokens; }
    public void setTransportTokens(Long transportTokens) { this.transportTokens = transportTokens; }
    public Long getHealthcareTokens() { return healthcareTokens; }
    public void setHealthcareTokens(Long healthcareTokens) { this.healthcareTokens = healthcareTokens; }
    public Long getEnvironmentTokens() { return environmentTokens; }
    public void setEnvironmentTokens(Long environmentTokens) { this.environmentTokens = environmentTokens; }
    public Long getSanitationTokens() { return sanitationTokens; }
    public void setSanitationTokens(Long sanitationTokens) { this.sanitationTokens = sanitationTokens; }
    public Long getEducationTokens() { return educationTokens; }
    public void setEducationTokens(Long educationTokens) { this.educationTokens = educationTokens; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }
}
