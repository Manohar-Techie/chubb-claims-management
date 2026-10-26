package com.claim.chubb.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssessmentResponse(
        Long id,
        String claimReference,
        BigDecimal assessedLiability,
        String currency,
        String rationale,
        String assessorOfficerIdentifier,
        LocalDateTime assessedAt
) {
}
