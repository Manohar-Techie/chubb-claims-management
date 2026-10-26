package com.claim.chubb.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        String claimReference,
        BigDecimal settlementAmount,
        String currency,
        String paymentReference,
        String processedByOfficerIdentifier,
        LocalDateTime settledAt
) {
}
