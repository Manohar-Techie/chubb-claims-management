package com.claim.chubb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignClaimRequest(

        @NotBlank(message = "Officer identifier is required")
        @Size(max = 100, message = "Officer identifier must not exceed 100 characters")
        String officerIdentifier

) {
}