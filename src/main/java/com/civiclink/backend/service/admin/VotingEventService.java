package com.civiclink.backend.service.admin;

import com.civiclink.backend.dto.admin.StartVotingEventRequest;
import com.civiclink.backend.entity.BudgetEvent;
import com.civiclink.backend.entity.User;
import com.civiclink.backend.repository.BudgetEventRepository;
import com.civiclink.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VotingEventService {
    private final BudgetEventRepository budgetEventRepository;
    private final UserRepository userRepository;

    public VotingEventService(BudgetEventRepository budgetEventRepository, UserRepository userRepository) {
        this.budgetEventRepository = budgetEventRepository;
        this.userRepository = userRepository;
    }

    public BudgetEvent current() {
        return budgetEventRepository.findTopByOrderByStartedAtDesc();
    }

    public BudgetEvent active() {
        return budgetEventRepository.findTopByActiveTrueOrderByStartedAtDesc();
    }

    public BudgetEvent start(StartVotingEventRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(admin.getRole())) {
            throw new IllegalArgumentException("Only admin can start voting event.");
        }

        BudgetEvent active = active();
        if (active != null) {
            active.setActive(false);
            budgetEventRepository.save(active);
        }

        BudgetEvent event = new BudgetEvent();
        event.setActive(true);
        event.setBudgetTokensGranted(request.budgetTokensPerCitizen());
        event.setProjectTokensGranted(request.projectTokensPerCitizen());
        event.setStartedBy(admin);
        BudgetEvent saved = budgetEventRepository.save(event);

        // Grant tokens to all citizens
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if ("citizen".equalsIgnoreCase(user.getRole())) {
                user.setBudgetTokens((user.getBudgetTokens() == null ? 0 : user.getBudgetTokens()) + request.budgetTokensPerCitizen());
                user.setProjectTokens((user.getProjectTokens() == null ? 0 : user.getProjectTokens()) + request.projectTokensPerCitizen());
            }
        }
        userRepository.saveAll(users);

        return saved;
    }

    public BudgetEvent stop(String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(admin.getRole())) {
            throw new IllegalArgumentException("Only admin can stop voting event.");
        }

        BudgetEvent active = active();
        if (active == null) {
            throw new IllegalArgumentException("No active voting event.");
        }
        active.setActive(false);
        budgetEventRepository.save(active);

        // Unused budget tokens from this event are forfeited when the event ends.
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if ("citizen".equalsIgnoreCase(user.getRole())) {
                user.setBudgetTokens(0L);
            }
        }
        userRepository.saveAll(users);

        return active;
    }
}
