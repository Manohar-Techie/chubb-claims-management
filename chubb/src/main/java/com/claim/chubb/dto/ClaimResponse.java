package com.claim.chubb.dto;

import com.claim.chubb.enums.ClaimStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimResponse(
        String claimReference,
        String claimantIdentifier,
        String claimType,
        String policyReference,
        LocalDate incidentDate,
        BigDecimal estimatedLoss,
        String currency,
        ClaimStatus status,
        LocalDateTime createdAt
) {}