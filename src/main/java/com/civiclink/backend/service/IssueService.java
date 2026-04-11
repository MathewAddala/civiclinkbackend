package com.civiclink.backend.service;

import com.civiclink.backend.dto.IssueCreateRequest;
import com.civiclink.backend.dto.IssueUpdateRequest;
import com.civiclink.backend.entity.Issue;
import com.civiclink.backend.entity.User;
import com.civiclink.backend.repository.IssueRepository;
import com.civiclink.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class IssueService {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public IssueService(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    public List<Issue> list(String email) {
        User actor = userRepository.findByEmail(email).orElse(null);
        if (actor != null && "admin".equalsIgnoreCase(actor.getRole())) {
            return issueRepository.findAllByOrderByCreatedAtDesc();
        } else if (actor != null) {
            return issueRepository.findBySubmittedBy_EmailOrderByCreatedAtDesc(email);
        }
        return List.of();
    }

    public Issue create(IssueCreateRequest request, String email) {
        return create(request, null, email);
    }

    public Issue create(IssueCreateRequest request, MultipartFile attachment, String email) {
        User submitter = userRepository.findByEmail(email).orElse(null);
        Issue issue = new Issue();
        issue.setTitle(request.title());
        issue.setDescription(request.description());
        issue.setLocation(request.location());
        issue.setLat(request.lat());
        issue.setLng(request.lng());
        issue.setPriority(request.priority() == null ? "Medium" : request.priority());
        issue.setSubmittedBy(submitter);
        if (submitter != null) {
            issue.setReporterName(submitter.getName());
            issue.setReporterEmail(submitter.getEmail());
        } else {
            issue.setReporterName("Unknown Citizen");
            issue.setReporterEmail(email);
        }
        if (attachment != null && !attachment.isEmpty()) {
            String contentType = attachment.getContentType() == null ? "" : attachment.getContentType().toLowerCase();
            if (!(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("application/pdf"))) {
                throw new IllegalArgumentException("Only JPG, PNG, and PDF attachments are allowed.");
            }
            if (attachment.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Attachment must be 5MB or smaller.");
            }
            try {
                issue.setAttachmentName(attachment.getOriginalFilename());
                issue.setAttachmentType(contentType);
                issue.setAttachmentData(Base64.getEncoder().encodeToString(attachment.getBytes()));
            } catch (IOException ex) {
                throw new IllegalArgumentException("Failed to read attachment.");
            }
        }
        return issueRepository.save(issue);
    }

    public Issue update(Long id, IssueUpdateRequest request) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found."));

        if (request.status() != null && !request.status().isBlank()) {
            issue.setStatus(request.status());
        }
        if (request.assignedTo() != null) {
            issue.setAssignedTo(request.assignedTo().isBlank() ? "Unassigned" : request.assignedTo());
        }
        if (request.priority() != null && !request.priority().isBlank()) {
            issue.setPriority(request.priority());
        }

        return issueRepository.save(issue);
    }
}
