package com.claim.chubb.dto;

import com.claim.chubb.enums.ClaimType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(
        @NotBlank
        @Size(max = 64)
        String claimantIdentifier,

        @NotNull
        ClaimType claimType,

        @NotBlank
        @Size(max = 64)
        String policyReference,

        @NotNull
        LocalDate incidentDate,

        @NotBlank
        @Size(max = 255)
        String incidentLocation,

        @NotBlank
        @Size(max = 4000)
        String description,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 17, fraction = 2)
        BigDecimal estimatedLoss,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}")
        String currency
) {
}
