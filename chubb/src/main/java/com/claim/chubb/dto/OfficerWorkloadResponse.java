package com.claim.chubb.dto;

public record OfficerWorkloadResponse(
        String officerIdentifier,
        long claimCount
) {
}
