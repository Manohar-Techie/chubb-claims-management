package com.claim.chubb.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(

        @NotBlank
        String claimantIdentifier,

        @NotBlank
        @Pattern(regexp = "MOTOR|PROPERTY")
        String claimType,

        @NotBlank
        String policyReference,

        @NotNull
        @PastOrPresent
        LocalDate incidentDate,

        @NotBlank
        String incidentLocation,

        @NotBlank
        String description,

        @NotNull
        @Positive
        BigDecimal estimatedLoss,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}")
        String currency

) {}