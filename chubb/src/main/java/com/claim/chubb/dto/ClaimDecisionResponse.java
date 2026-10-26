package com.claim.chubb.dto;

import com.claim.chubb.enums.DecisionValue;

import java.time.LocalDateTime;

public record ClaimDecisionResponse(
        Long id,
        String claimReference,
        DecisionValue decision,
        String decisionMakerIdentifier,
        String decisionReason,
        LocalDateTime decidedAt
) {
}
