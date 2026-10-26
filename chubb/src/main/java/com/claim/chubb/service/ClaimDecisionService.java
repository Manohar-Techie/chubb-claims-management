package com.claim.chubb.service;

import com.claim.chubb.dto.ClaimDecisionResponse;
import com.claim.chubb.dto.CreateClaimDecisionRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimDecision;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.enums.DecisionValue;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimDecisionRepository;
import com.claim.chubb.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimDecisionService {

    private final ClaimRepository claimRepository;
    private final ClaimDecisionRepository decisionRepository;
    private final AuditService auditService;

    public ClaimDecisionService(ClaimRepository claimRepository,
                                ClaimDecisionRepository decisionRepository,
                                AuditService auditService) {
        this.claimRepository = claimRepository;
        this.decisionRepository = decisionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ClaimDecisionResponse makeDecision(
            String claimReference, CreateClaimDecisionRequest request) {

        Claim claim = findClaim(claimReference);
        if (claim.getStatus() != ClaimStatus.ASSESSED) {
            throw new InvalidClaimStateException(
                    "Only assessed claims can receive a decision");
        }

        ClaimDecision decision = new ClaimDecision(
                claim,
                request.decision(),
                request.decisionMakerIdentifier().trim(),
                request.decisionReason().trim());
        ClaimDecision savedDecision = decisionRepository.save(decision);

        claim.setStatus(request.decision() == DecisionValue.APPROVE
                ? ClaimStatus.APPROVED
                : ClaimStatus.REJECTED);
        claimRepository.save(claim);
        auditService.recordEvent(
                claimReference,
                request.decision() == DecisionValue.APPROVE
                        ? "CLAIM_APPROVED"
                        : "CLAIM_REJECTED",
                request.decisionMakerIdentifier().trim(),
                ClaimStatus.ASSESSED,
                claim.getStatus(),
                "Decision record " + savedDecision.getId()
                        + ": " + savedDecision.getDecisionReason());

        return toResponse(savedDecision);
    }

    @Transactional(readOnly = true)
    public List<ClaimDecisionResponse> getDecisionHistory(String claimReference) {
        findClaim(claimReference);
        return decisionRepository
                .findByClaimClaimReferenceOrderByDecidedAtAscIdAsc(claimReference)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Claim findClaim(String claimReference) {
        return claimRepository.findByClaimReference(claimReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Claim not found: " + claimReference));
    }

    private ClaimDecisionResponse toResponse(ClaimDecision decision) {
        return new ClaimDecisionResponse(
                decision.getId(),
                decision.getClaim().getClaimReference(),
                decision.getDecision(),
                decision.getDecisionMakerIdentifier(),
                decision.getDecisionReason(),
                decision.getDecidedAt());
    }
}
