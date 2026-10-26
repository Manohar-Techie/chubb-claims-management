package com.claim.chubb.service;

import com.claim.chubb.dto.CreateSettlementRequest;
import com.claim.chubb.dto.SettlementResponse;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimSettlement;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimRepository;
import com.claim.chubb.repository.ClaimSettlementRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SettlementService {

    private final ClaimRepository claimRepository;
    private final ClaimSettlementRepository settlementRepository;
    private final AuditService auditService;

    public SettlementService(ClaimRepository claimRepository,
                             ClaimSettlementRepository settlementRepository,
                             AuditService auditService) {
        this.claimRepository = claimRepository;
        this.settlementRepository = settlementRepository;
        this.auditService = auditService;
    }

    @Transactional
    public SettlementResponse settleClaim(
            String claimReference, CreateSettlementRequest request) {

        Claim claim = findClaim(claimReference);
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new InvalidClaimStateException(
                    "Only approved claims can be settled");
        }
        if (settlementRepository.existsByClaimClaimReference(claimReference)) {
            throw new InvalidClaimStateException(
                    "Claim already has a settlement");
        }
        String paymentReference = request.paymentReference().trim();
        if (settlementRepository.existsByPaymentReference(paymentReference)) {
            throw new InvalidClaimStateException(
                    "Payment reference already exists");
        }
        if (claim.getAssessedLiability() == null
                || request.settlementAmount().compareTo(claim.getAssessedLiability()) != 0) {
            throw new InvalidClaimStateException(
                    "Settlement amount must equal the assessed liability");
        }
        if (!claim.getCurrency().equals(request.currency())) {
            throw new InvalidClaimStateException(
                    "Settlement currency must match the claim currency");
        }

        ClaimSettlement settlement = new ClaimSettlement(
                claim,
                request.settlementAmount(),
                request.currency(),
                paymentReference,
                request.processedByOfficerIdentifier().trim());
        ClaimSettlement savedSettlement;
        try {
            savedSettlement = settlementRepository.saveAndFlush(settlement);
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidClaimStateException(
                    "A settlement already exists for this claim or payment reference");
        }

        claim.setStatus(ClaimStatus.SETTLED);
        claimRepository.save(claim);
        auditService.recordEvent(
                claimReference,
                "CLAIM_SETTLED",
                request.processedByOfficerIdentifier().trim(),
                ClaimStatus.APPROVED,
                ClaimStatus.SETTLED,
                "Settlement " + savedSettlement.getId() + ": "
                        + request.settlementAmount() + " " + request.currency()
                        + "; paymentReference=" + paymentReference);

        return toResponse(savedSettlement);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlements(String claimReference) {
        findClaim(claimReference);
        return settlementRepository
                .findByClaimClaimReferenceOrderBySettledAtAscIdAsc(claimReference)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Claim findClaim(String claimReference) {
        return claimRepository.findByClaimReference(claimReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Claim not found: " + claimReference));
    }

    private SettlementResponse toResponse(ClaimSettlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getClaim().getClaimReference(),
                settlement.getSettlementAmount(),
                settlement.getCurrency(),
                settlement.getPaymentReference(),
                settlement.getProcessedByOfficerIdentifier(),
                settlement.getSettledAt());
    }
}
