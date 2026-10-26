package com.claim.chubb.dto;

import com.claim.chubb.enums.ClaimStatus;

import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalClaims,
        Map<ClaimStatus, Long> claimsByStatus,
        List<OfficerWorkloadResponse> officerWorkload,
        List<CurrencyLiabilityResponse> outstandingLiabilityByCurrency
) {
}
