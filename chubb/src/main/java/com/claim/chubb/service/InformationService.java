package com.claim.chubb.service;

import com.claim.chubb.dto.ClaimInformationRequestResponse;
import com.claim.chubb.dto.ProvideInformationRequest;
import com.claim.chubb.dto.RequestInformationRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimInformationRequest;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimInformationRequestRepository;
import com.claim.chubb.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InformationService {

    private final ClaimRepository claimRepository;
    private final ClaimInformationRequestRepository informationRequestRepository;
    private final AuditService auditService;

    public InformationService(ClaimRepository claimRepository,
                              ClaimInformationRequestRepository informationRequestRepository,
                              AuditService auditService) {
        this.claimRepository = claimRepository;
        this.informationRequestRepository = informationRequestRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ClaimInformationRequestResponse requestInformation(
            String claimReference, RequestInformationRequest request) {

        Claim claim = findClaim(claimReference);
        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new InvalidClaimStateException(
                    "Information can only be requested for claims under review");
        }

        ClaimInformationRequest informationRequest = new ClaimInformationRequest(
                claim, request.description().trim());
        ClaimInformationRequest savedRequest =
                informationRequestRepository.save(informationRequest);

        claim.setStatus(ClaimStatus.INFORMATION_REQUIRED);
        claimRepository.save(claim);
        auditService.recordEvent(
                claimReference,
                "INFORMATION_REQUESTED",
                "SYSTEM",
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.INFORMATION_REQUIRED,
                "Information request " + savedRequest.getId() + ": "
                        + savedRequest.getDescription());

        return toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ClaimInformationRequestResponse> getInformationRequests(
            String claimReference) {

        findClaim(claimReference);
        return informationRequestRepository
                .findByClaimClaimReferenceOrderByRequestedAtAsc(claimReference)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ClaimInformationRequestResponse provideInformation(
            String claimReference, Long requestId, ProvideInformationRequest request) {

        Claim claim = findClaim(claimReference);
        ClaimInformationRequest informationRequest = informationRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Information request not found: " + requestId));

        if (!informationRequest.getClaim().getClaimReference().equals(claimReference)) {
            throw new ResourceNotFoundException(
                    "Information request not found for claim: " + claimReference);
        }
        if (claim.getStatus() != ClaimStatus.INFORMATION_REQUIRED) {
            throw new InvalidClaimStateException(
                    "Claim must require information before a response can be provided");
        }
        if (informationRequest.getResponse() != null) {
            throw new InvalidClaimStateException(
                    "Information request has already been answered");
        }

        informationRequest.provideResponse(request.response().trim());
        ClaimInformationRequest savedRequest =
                informationRequestRepository.save(informationRequest);

        if (informationRequestRepository
                .countByClaimClaimReferenceAndResponseIsNull(claimReference) == 0) {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        }
        claimRepository.save(claim);
        auditService.recordEvent(
                claimReference,
                "INFORMATION_PROVIDED",
                "SYSTEM",
                ClaimStatus.INFORMATION_REQUIRED,
                claim.getStatus(),
                "Response provided for information request " + requestId);

        return toResponse(savedRequest);
    }

    private Claim findClaim(String claimReference) {
        return claimRepository.findByClaimReference(claimReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Claim not found: " + claimReference));
    }

    private ClaimInformationRequestResponse toResponse(
            ClaimInformationRequest informationRequest) {

        return new ClaimInformationRequestResponse(
                informationRequest.getId(),
                informationRequest.getDescription(),
                informationRequest.getRequestedAt(),
                informationRequest.getResponse(),
                informationRequest.getResponseAt());
    }
}
