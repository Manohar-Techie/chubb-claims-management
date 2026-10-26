package com.claim.chubb.service;

import com.claim.chubb.dto.ClaimResponse;
import com.claim.chubb.dto.CreateClaimRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;

    public ClaimService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional
    public ClaimResponse submitClaim(CreateClaimRequest request) {

        Claim claim = new Claim();

        claim.setClaimantIdentifier(request.claimantIdentifier());
        claim.setClaimType(request.claimType());
        claim.setPolicyReference(request.policyReference());
        claim.setIncidentDate(request.incidentDate());
        claim.setIncidentLocation(request.incidentLocation());
        claim.setDescription(request.description());
        claim.setEstimatedLoss(request.estimatedLoss());
        claim.setCurrency(request.currency());

        // Status defaults to SUBMITTED.
        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaim(String claimReference) {

        Claim claim = claimRepository
                .findByClaimReference(claimReference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim not found: " + claimReference));

        return toResponse(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getAllClaims() {

        return claimRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByStatus(
            ClaimStatus status) {

        return claimRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ClaimResponse toResponse(Claim claim) {

        return new ClaimResponse(
                claim.getClaimReference(),
                claim.getClaimantIdentifier(),
                claim.getClaimType(),
                claim.getPolicyReference(),
                claim.getIncidentDate(),
                claim.getEstimatedLoss(),
                claim.getCurrency(),
                claim.getStatus(),
                claim.getCreatedAt()
        );
    }
}