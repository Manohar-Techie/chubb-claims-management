package com.claim.chubb.dto;

import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.enums.ClaimType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimResponse(
        String claimReference,
        ClaimType claimType,
        String marketCode,
        String policyReference,
        LocalDate incidentDate,
        String incidentLocation,
        String description,
        BigDecimal estimatedLoss,
        String currency,
        BigDecimal assessedLiability,
        ClaimStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
