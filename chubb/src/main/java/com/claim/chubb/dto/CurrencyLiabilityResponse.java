package com.claim.chubb.dto;

import java.math.BigDecimal;

public record CurrencyLiabilityResponse(
        String currency,
        BigDecimal outstandingLiability
) {
}
