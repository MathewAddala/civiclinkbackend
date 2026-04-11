package com.civiclink.backend.service;

import com.civiclink.backend.dto.dashboard.ActivityItemResponse;
import com.civiclink.backend.dto.dashboard.DashboardSummaryResponse;
import com.civiclink.backend.dto.dashboard.IssueTrendPointResponse;
import com.civiclink.backend.entity.Issue;
import com.civiclink.backend.repository.IssueRepository;
import com.civiclink.backend.repository.ProjectRepository;
import com.civiclink.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final BudgetService budgetService;

    public DashboardService(UserRepository userRepository,
                            IssueRepository issueRepository,
                            ProjectRepository projectRepository,
                            BudgetService budgetService) {
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.budgetService = budgetService;
    }

    public DashboardSummaryResponse summary() {
        long totalUsers = userRepository.count();
        long issuesReported = issueRepository.count();
        long issuesResolved = issueRepository.countByStatus("resolved");
        long projectsSupported = projectRepository.countByStatus("funding") + projectRepository.countByStatus("approved");
        long pendingBudgets = budgetService.pendingCount();

        String civicScore = String.valueOf(Math.max(500, 700 + (int) issuesResolved * 2));
        return new DashboardSummaryResponse(totalUsers, issuesReported, issuesResolved, projectsSupported, pendingBudgets, civicScore);
    }

    public List<ActivityItemResponse> activity() {
        List<ActivityItemResponse> items = new ArrayList<>();

        for (Issue issue : issueRepository.findTop20ByOrderByCreatedAtDesc()) {
            items.add(new ActivityItemResponse(
                    "issue",
                    String.format("Issue %s: %s (%s)", issue.getId(), issue.getTitle(), issue.getStatus()),
                    issue.getCreatedAt() == null ? Instant.now() : issue.getCreatedAt()
            ));
        }

        return items.stream()
                .sorted(Comparator.comparing(ActivityItemResponse::createdAt).reversed())
                .limit(12)
                .toList();
    }

    public List<IssueTrendPointResponse> issueTrends() {
        List<Issue> issues = issueRepository.findAll();
        List<IssueTrendPointResponse> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            long reported = issues.stream().filter(issue -> sameMonth(issue.getCreatedAt(), month)).count();
            long resolved = issues.stream()
                    .filter(issue -> sameMonth(issue.getCreatedAt(), month))
                    .filter(issue -> "resolved".equalsIgnoreCase(issue.getStatus()))
                    .count();
            String name = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(new IssueTrendPointResponse(name, reported, resolved));
        }

        return result;
    }

    private boolean sameMonth(Instant createdAt, YearMonth month) {
        if (createdAt == null) return false;
        YearMonth issueMonth = YearMonth.from(createdAt.atZone(ZoneOffset.UTC));
        return issueMonth.equals(month);
    }
}
