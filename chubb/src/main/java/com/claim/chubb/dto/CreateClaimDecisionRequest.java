package com.claim.chubb.dto;

import com.claim.chubb.enums.DecisionValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateClaimDecisionRequest(
        @NotNull(message = "Decision is required")
        DecisionValue decision,

        @NotBlank(message = "Decision-maker identifier is required")
        @Size(max = 100, message = "Decision-maker identifier must not exceed 100 characters")
        String decisionMakerIdentifier,

        @NotBlank(message = "Decision reason is required")
        @Size(max = 2000, message = "Decision reason must not exceed 2000 characters")
        String decisionReason
) {
}
