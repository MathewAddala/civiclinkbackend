package com.civiclink.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private Double goalAmount;
    private Double currentAmount;
    private String status;
    private String color;
    private String sector;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "pending";
        if (color == null) color = "blue";
        if (sector == null) sector = "Other";
        if (goalAmount == null) goalAmount = 0.0;
        if (currentAmount == null) currentAmount = 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getGoalAmount() { return goalAmount; }
    public void setGoalAmount(Double goalAmount) { this.goalAmount = goalAmount; }
    public Double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(Double currentAmount) { this.currentAmount = currentAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
}
