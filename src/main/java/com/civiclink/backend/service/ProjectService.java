package com.civiclink.backend.service;

import com.civiclink.backend.dto.ProjectCreateRequest;
import com.civiclink.backend.dto.ProjectSupportRequest;
import com.civiclink.backend.entity.Project;
import com.civiclink.backend.entity.User;
import com.civiclink.backend.repository.ProjectRepository;
import com.civiclink.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Project> list() {
        return projectRepository.findAll();
    }

    public Project create(ProjectCreateRequest request, String email) {
        userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));

        Project project = new Project();
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setGoalAmount(request.goalAmount());
        project.setCurrentAmount(0.0);
        project.setStatus("pending");
        project.setSector(request.sector());
        project.setColor(colorForSector(request.sector()));
        return projectRepository.save(project);
    }

    private String colorForSector(String sector) {
        if (sector == null) return "indigo";
        return switch (sector.trim().toLowerCase()) {
            case "transportation" -> "blue";
            case "healthcare" -> "green";
            case "environment" -> "yellow";
            case "sanitation" -> "purple";
            case "education" -> "red";
            default -> "indigo";
        };
    }

    public Project support(Long id, ProjectSupportRequest request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
        Project project = projectRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found."));
        double amount = request.amount() == null ? 100.0 : Math.max(1.0, request.amount());

        long projectBalance = user.getProjectTokens() == null ? 0 : user.getProjectTokens();
        if (projectBalance < (long) amount) {
            throw new IllegalArgumentException("Not enough project tokens to support this project.");
        }
        user.setProjectTokens(projectBalance - (long) amount);
        userRepository.save(user);

        project.setCurrentAmount((project.getCurrentAmount() == null ? 0.0 : project.getCurrentAmount()) + amount);
        if ("pending".equalsIgnoreCase(project.getStatus())) {
            project.setStatus("funding");
        }
        if (project.getGoalAmount() != null
                && project.getCurrentAmount() >= project.getGoalAmount()
                && !"approved".equalsIgnoreCase(project.getStatus())) {
            // Fully funded means ready for admin review, not auto-approval.
            project.setStatus("funding");
        }
        return projectRepository.save(project);
    }

    public Project approve(Long id, String email) {
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(actor.getRole())) {
            throw new IllegalArgumentException("Only admin can approve projects.");
        }
        Project project = projectRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found."));
        project.setStatus("approved");
        return projectRepository.save(project);
    }

    public Project reject(Long id, String email) {
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(actor.getRole())) {
            throw new IllegalArgumentException("Only admin can reject projects.");
        }
        Project project = projectRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found."));
        project.setStatus("rejected");
        return projectRepository.save(project);
    }
}
