package com.civiclink.backend.service;

import com.civiclink.backend.dto.budget.BudgetProposalRequest;
import com.civiclink.backend.dto.budget.BudgetProposalResponse;
import com.civiclink.backend.entity.BudgetProposal;
import com.civiclink.backend.entity.User;
import com.civiclink.backend.repository.BudgetEventRepository;
import com.civiclink.backend.repository.BudgetProposalRepository;
import com.civiclink.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {
    private final BudgetProposalRepository budgetProposalRepository;
    private final UserRepository userRepository;
    private final BudgetEventRepository budgetEventRepository;

    public BudgetService(BudgetProposalRepository budgetProposalRepository,
                         UserRepository userRepository,
                         BudgetEventRepository budgetEventRepository) {
        this.budgetProposalRepository = budgetProposalRepository;
        this.userRepository = userRepository;
        this.budgetEventRepository = budgetEventRepository;
    }

    public BudgetProposalResponse getCurrentAllocation() {
        BudgetProposal approved = budgetProposalRepository.findTopByStatusOrderByCreatedAtDesc("approved");
        if (approved == null) {
            return new BudgetProposalResponse(null, 20, 20, 20, 20, 20, 0L, 0L, 0L, 0L, 0L, 0L, "default", null, "system");
        }
        return map(approved);
    }

    public BudgetProposalResponse submit(BudgetProposalRequest request, String email) {
        var activeEvent = budgetEventRepository.findTopByActiveTrueOrderByStartedAtDesc();
        if (activeEvent == null) {
            throw new IllegalArgumentException("Budget voting is not active. Wait for admin to start the event.");
        }

        long transportTokens = request.transportTokens();
        long healthcareTokens = request.healthcareTokens();
        long environmentTokens = request.environmentTokens();
        long sanitationTokens = request.sanitationTokens();
        long educationTokens = request.educationTokens();
        long totalTokens = transportTokens + healthcareTokens + environmentTokens + sanitationTokens + educationTokens;
        if (totalTokens < 1) {
            throw new IllegalArgumentException("You must allocate at least 1 token to vote.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        long balance = user.getBudgetTokens() == null ? 0 : user.getBudgetTokens();
        if (balance < totalTokens) {
            throw new IllegalArgumentException("Not enough budget tokens to vote. Reduce allocations or ask admin to grant tokens.");
        }
        if (balance > 0 && totalTokens != balance) {
            throw new IllegalArgumentException("You must allocate your entire budget token balance in this vote (remaining: " + balance + "). Adjust sliders so the total equals your balance.");
        }
        user.setBudgetTokens(balance - totalTokens);
        userRepository.save(user);

        BudgetProposal proposal = new BudgetProposal();
        proposal.setTransportTokens(transportTokens);
        proposal.setHealthcareTokens(healthcareTokens);
        proposal.setEnvironmentTokens(environmentTokens);
        proposal.setSanitationTokens(sanitationTokens);
        proposal.setEducationTokens(educationTokens);
        proposal.setTokensSpent(totalTokens);

        // Store a snapshot % allocation for easy charting/reading
        int pctTransport = (int) Math.round((transportTokens * 100.0) / totalTokens);
        int pctHealthcare = (int) Math.round((healthcareTokens * 100.0) / totalTokens);
        int pctEnvironment = (int) Math.round((environmentTokens * 100.0) / totalTokens);
        int pctSanitation = (int) Math.round((sanitationTokens * 100.0) / totalTokens);
        int pctEducation = 100 - (pctTransport + pctHealthcare + pctEnvironment + pctSanitation);

        proposal.setTransport(pctTransport);
        proposal.setHealthcare(pctHealthcare);
        proposal.setEnvironment(pctEnvironment);
        proposal.setSanitation(pctSanitation);
        proposal.setEducation(pctEducation);
        proposal.setStatus("pending");
        proposal.setSubmittedBy(user);

        return map(budgetProposalRepository.save(proposal));
    }

    public List<BudgetProposalResponse> listRecent() {
        return budgetProposalRepository.findTop20ByOrderByCreatedAtDesc().stream().map(this::map).toList();
    }

    public BudgetProposalResponse approve(Long id, String email) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(actor.getRole())) {
            throw new IllegalArgumentException("Only admin can approve proposals.");
        }

        BudgetProposal proposal = budgetProposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found."));
        proposal.setStatus("approved");

        return map(budgetProposalRepository.save(proposal));
    }

    public BudgetProposalResponse approveByAverage(String email) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(actor.getRole())) {
            throw new IllegalArgumentException("Only admin can approve budget.");
        }

        List<BudgetProposal> pending = budgetProposalRepository.findByStatusOrderByCreatedAtDesc("pending");
        if (pending.isEmpty()) {
            throw new IllegalArgumentException("No pending citizen proposals available.");
        }

        var activeEvent = budgetEventRepository.findTopByActiveTrueOrderByStartedAtDesc();
        if (activeEvent == null) {
            throw new IllegalArgumentException("No active voting event. Cannot approve or override the budget.");
        }
        activeEvent.setActive(false);
        budgetEventRepository.save(activeEvent);

        long sumTransport = pending.stream().mapToLong(p -> p.getTransportTokens() == null ? 0L : p.getTransportTokens()).sum();
        long sumHealthcare = pending.stream().mapToLong(p -> p.getHealthcareTokens() == null ? 0L : p.getHealthcareTokens()).sum();
        long sumEnvironment = pending.stream().mapToLong(p -> p.getEnvironmentTokens() == null ? 0L : p.getEnvironmentTokens()).sum();
        long sumSanitation = pending.stream().mapToLong(p -> p.getSanitationTokens() == null ? 0L : p.getSanitationTokens()).sum();
        long sumEducation = pending.stream().mapToLong(p -> p.getEducationTokens() == null ? 0L : p.getEducationTokens()).sum();
        long totalTokens = sumTransport + sumHealthcare + sumEnvironment + sumSanitation + sumEducation;
        if (totalTokens < 1) {
            throw new IllegalArgumentException("Pending proposals have no token allocations.");
        }

        int avgTransport = (int) Math.round((sumTransport * 100.0) / totalTokens);
        int avgHealthcare = (int) Math.round((sumHealthcare * 100.0) / totalTokens);
        int avgEnvironment = (int) Math.round((sumEnvironment * 100.0) / totalTokens);
        int avgSanitation = (int) Math.round((sumSanitation * 100.0) / totalTokens);
        int avgEducation = 100 - (avgTransport + avgHealthcare + avgEnvironment + avgSanitation);

        int total = avgTransport + avgHealthcare + avgEnvironment + avgSanitation + avgEducation;
        // Normalize to 100 by adjusting education as the balancing sector.
        avgEducation += (100 - total);

        BudgetProposal approved = new BudgetProposal();
        approved.setTransport(avgTransport);
        approved.setHealthcare(avgHealthcare);
        approved.setEnvironment(avgEnvironment);
        approved.setSanitation(avgSanitation);
        approved.setEducation(Math.max(0, Math.min(100, avgEducation)));
        approved.setTokensSpent(totalTokens);
        approved.setTransportTokens(sumTransport);
        approved.setHealthcareTokens(sumHealthcare);
        approved.setEnvironmentTokens(sumEnvironment);
        approved.setSanitationTokens(sumSanitation);
        approved.setEducationTokens(sumEducation);
        approved.setStatus("approved");
        approved.setSubmittedBy(actor);
        BudgetProposal saved = budgetProposalRepository.save(approved);

        pending.forEach(p -> p.setStatus("reviewed"));
        budgetProposalRepository.saveAll(pending);

        return map(saved);
    }

    public BudgetProposalResponse overrideCurrent(BudgetProposalRequest request, String email) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(actor.getRole())) {
            throw new IllegalArgumentException("Only admin can override budget.");
        }

        var activeEvent = budgetEventRepository.findTopByActiveTrueOrderByStartedAtDesc();
        if (activeEvent == null) {
            throw new IllegalArgumentException("No active voting event. Cannot approve or override the budget.");
        }
        activeEvent.setActive(false);
        budgetEventRepository.save(activeEvent);

        long transportTokens = request.transportTokens();
        long healthcareTokens = request.healthcareTokens();
        long environmentTokens = request.environmentTokens();
        long sanitationTokens = request.sanitationTokens();
        long educationTokens = request.educationTokens();
        long totalTokens = transportTokens + healthcareTokens + environmentTokens + sanitationTokens + educationTokens;
        if (totalTokens < 1) {
            throw new IllegalArgumentException("Override requires at least 1 token allocated.");
        }

        int pctTransport = (int) Math.round((transportTokens * 100.0) / totalTokens);
        int pctHealthcare = (int) Math.round((healthcareTokens * 100.0) / totalTokens);
        int pctEnvironment = (int) Math.round((environmentTokens * 100.0) / totalTokens);
        int pctSanitation = (int) Math.round((sanitationTokens * 100.0) / totalTokens);
        int pctEducation = 100 - (pctTransport + pctHealthcare + pctEnvironment + pctSanitation);

        BudgetProposal proposal = new BudgetProposal();
        proposal.setTransportTokens(transportTokens);
        proposal.setHealthcareTokens(healthcareTokens);
        proposal.setEnvironmentTokens(environmentTokens);
        proposal.setSanitationTokens(sanitationTokens);
        proposal.setEducationTokens(educationTokens);
        proposal.setTokensSpent(totalTokens);

        proposal.setTransport(pctTransport);
        proposal.setHealthcare(pctHealthcare);
        proposal.setEnvironment(pctEnvironment);
        proposal.setSanitation(pctSanitation);
        proposal.setEducation(pctEducation);
        proposal.setStatus("approved");
        proposal.setSubmittedBy(actor);

        return map(budgetProposalRepository.save(proposal));
    }

    public long pendingCount() {
        return budgetProposalRepository.findByStatusOrderByCreatedAtDesc("pending").stream()
                .filter(p -> "pending".equalsIgnoreCase(p.getStatus()))
                .count();
    }

    private BudgetProposalResponse map(BudgetProposal proposal) {
        return new BudgetProposalResponse(
                proposal.getId(),
                proposal.getTransport(),
                proposal.getHealthcare(),
                proposal.getEnvironment(),
                proposal.getSanitation(),
                proposal.getEducation(),
                proposal.getTokensSpent() == null ? 0L : proposal.getTokensSpent(),
                proposal.getTransportTokens() == null ? 0L : proposal.getTransportTokens(),
                proposal.getHealthcareTokens() == null ? 0L : proposal.getHealthcareTokens(),
                proposal.getEnvironmentTokens() == null ? 0L : proposal.getEnvironmentTokens(),
                proposal.getSanitationTokens() == null ? 0L : proposal.getSanitationTokens(),
                proposal.getEducationTokens() == null ? 0L : proposal.getEducationTokens(),
                proposal.getStatus(),
                proposal.getCreatedAt(),
                proposal.getSubmittedBy() != null ? proposal.getSubmittedBy().getName() : "system"
        );
    }
}
