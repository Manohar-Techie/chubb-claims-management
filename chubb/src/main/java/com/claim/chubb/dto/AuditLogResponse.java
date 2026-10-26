package com.claim.chubb.dto;

import com.claim.chubb.enums.ClaimStatus;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String claimReference,
        String action,
        String performedByIdentifier,
        ClaimStatus previousStatus,
        ClaimStatus newStatus,
        String details,
        LocalDateTime occurredAt
) {
}
