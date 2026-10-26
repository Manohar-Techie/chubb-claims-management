package com.claim.chubb.service;

import com.claim.chubb.dto.AssessmentResponse;
import com.claim.chubb.dto.CreateAssessmentRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimAssessment;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimAssessmentRepository;
import com.claim.chubb.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssessmentService {

    private final ClaimRepository claimRepository;
    private final ClaimAssessmentRepository assessmentRepository;

    public AssessmentService(ClaimRepository claimRepository,
                             ClaimAssessmentRepository assessmentRepository) {
        this.claimRepository = claimRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional
    public AssessmentResponse assessClaim(
            String claimReference, CreateAssessmentRequest request) {

        Claim claim = findClaim(claimReference);
        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new InvalidClaimStateException(
                    "Only claims under review can be assessed");
        }

        ClaimAssessment assessment = new ClaimAssessment(
                claim,
                request.assessedLiability(),
                request.currency(),
                request.rationale().trim(),
                request.assessorOfficerIdentifier().trim());
        ClaimAssessment savedAssessment = assessmentRepository.save(assessment);

        claim.setAssessedLiability(request.assessedLiability());
        claim.setStatus(ClaimStatus.ASSESSED);
        claimRepository.save(claim);

        return toResponse(savedAssessment);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAssessmentHistory(String claimReference) {
        findClaim(claimReference);
        return assessmentRepository
                .findByClaimClaimReferenceOrderByAssessedAtAscIdAsc(claimReference)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Claim findClaim(String claimReference) {
        return claimRepository.findByClaimReference(claimReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Claim not found: " + claimReference));
    }

    private AssessmentResponse toResponse(ClaimAssessment assessment) {
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getClaim().getClaimReference(),
                assessment.getAssessedLiability(),
                assessment.getCurrency(),
                assessment.getRationale(),
                assessment.getAssessorOfficerIdentifier(),
                assessment.getAssessedAt());
    }
}
