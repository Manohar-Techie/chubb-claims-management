package com.claim.chubb.service;

import com.claim.chubb.dto.AuditLogResponse;
import com.claim.chubb.entity.AuditLog;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.AuditLogRepository;
import com.claim.chubb.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ClaimRepository claimRepository;

    public AuditService(AuditLogRepository auditLogRepository,
                        ClaimRepository claimRepository) {
        this.auditLogRepository = auditLogRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional
    public void recordEvent(String claimReference, String action, String performedByIdentifier,
                            ClaimStatus previousStatus, ClaimStatus newStatus, String details) {
        auditLogRepository.save(new AuditLog(
                claimReference,
                action,
                performedByIdentifier,
                previousStatus,
                newStatus,
                details));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditHistory(String claimReference) {
        if (!claimRepository.existsByClaimReference(claimReference)) {
            throw new ResourceNotFoundException(
                    "Claim not found: " + claimReference);
        }

        return auditLogRepository
                .findByClaimReferenceOrderByOccurredAtAscIdAsc(claimReference)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getClaimReference(),
                auditLog.getAction(),
                auditLog.getPerformedByIdentifier(),
                auditLog.getPreviousStatus(),
                auditLog.getNewStatus(),
                auditLog.getDetails(),
                auditLog.getOccurredAt());
    }
}
