package com.claim.chubb.service;

import com.claim.chubb.dto.AssignClaimRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimAssignment;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimAssignmentRepository;
import com.claim.chubb.repository.ClaimRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentService {

    private final ClaimRepository claimRepository;
    private final ClaimAssignmentRepository assignmentRepository;
    private final AuditService auditService;

    public AssignmentService(
            ClaimRepository claimRepository,
            ClaimAssignmentRepository assignmentRepository,
            AuditService auditService) {

        this.claimRepository = claimRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Claim assignClaim(
            String claimReference,
            AssignClaimRequest request) {

        Claim claim = claimRepository
                .findByClaimReference(claimReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim not found: " + claimReference));

        ClaimStatus currentStatus = claim.getStatus();

        if (currentStatus != ClaimStatus.SUBMITTED
                && currentStatus != ClaimStatus.UNDER_REVIEW) {

            throw new InvalidClaimStateException(
                    "Only submitted or under-review claims can be assigned");
        }

        // Update the current officer.
        claim.setAssignedOfficerIdentifier(
                request.officerIdentifier().trim());

        // Move newly assigned claims into review.
        if (currentStatus == ClaimStatus.SUBMITTED) {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        }

        // Save the claim update.
        Claim savedClaim = claimRepository.save(claim);

        // Preserve the assignment history.
        ClaimAssignment assignment = new ClaimAssignment(
                savedClaim,
                request.officerIdentifier().trim());

        assignmentRepository.save(assignment);
        auditService.recordEvent(
                savedClaim.getClaimReference(),
                "CLAIM_ASSIGNED",
                request.officerIdentifier().trim(),
                currentStatus,
                savedClaim.getStatus(),
                "Assigned officer: " + request.officerIdentifier().trim());

        return savedClaim;
    }

    @Transactional(readOnly = true)
    public List<ClaimAssignment> getAssignmentHistory(
            String claimReference) {

        if (!claimRepository.existsByClaimReference(claimReference)) {
            throw new ResourceNotFoundException(
                    "Claim not found: " + claimReference);
        }

        return assignmentRepository
                .findByClaimClaimReferenceOrderByAssignedAtDesc(
                        claimReference);
    }
}