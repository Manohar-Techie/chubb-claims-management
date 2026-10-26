package com.claim.chubb.service;

import com.claim.chubb.dto.ClaimResponse;
import com.claim.chubb.dto.CreateClaimRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
public class ClaimService {

    private final ClaimRepository claimRepository;

    public ClaimService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional
    public ClaimResponse submitClaim(
            CreateClaimRequest request,
            @NotBlank @Size(max = 16) String marketCode
    ) {
        Claim claim = new Claim();
        claim.setClaimReference(UUID.randomUUID().toString());
        claim.setClaimantIdentifier(request.claimantIdentifier());
        claim.setClaimType(request.claimType());
        claim.setMarketCode(marketCode);
        claim.setPolicyReference(request.policyReference());
        claim.setIncidentDate(request.incidentDate());
        claim.setIncidentLocation(request.incidentLocation());
        claim.setDescription(request.description());
        claim.setEstimatedLoss(request.estimatedLoss());
        claim.setCurrency(request.currency());
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setAssessedLiability(null);
        claim.setAssignedOfficerIdentifier(null);

        return toResponse(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimByReference(String claimReference) {
        Claim claim = claimRepository.findByClaimReference(claimReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Claim not found with reference: " + claimReference
                ));
        return toResponse(claim);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> listClaims(ClaimStatus status, Pageable pageable) {
        Page<Claim> claims = status == null
                ? claimRepository.findAll(pageable)
                : claimRepository.findByStatus(status, pageable);
        return claims.map(this::toResponse);
    }

    private ClaimResponse toResponse(Claim claim) {
        return new ClaimResponse(
                claim.getClaimReference(),
                claim.getClaimType(),
                claim.getMarketCode(),
                claim.getPolicyReference(),
                claim.getIncidentDate(),
                claim.getIncidentLocation(),
                claim.getDescription(),
                claim.getEstimatedLoss(),
                claim.getCurrency(),
                claim.getAssessedLiability(),
                claim.getStatus(),
                claim.getCreatedAt(),
                claim.getUpdatedAt()
        );
    }
}
