package com.claim.chubb.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAssessmentRequest(
        @NotNull(message = "Assessed liability is required")
        @DecimalMin(value = "0.0", inclusive = true,
                message = "Assessed liability must be zero or greater")
        @Digits(integer = 13, fraction = 2,
                message = "Assessed liability must have at most 13 integer and 2 fractional digits")
        BigDecimal assessedLiability,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be three uppercase letters")
        String currency,

        @NotBlank(message = "Rationale is required")
        @Size(max = 2000, message = "Rationale must not exceed 2000 characters")
        String rationale,

        @NotBlank(message = "Assessor officer identifier is required")
        @Size(max = 100, message = "Assessor officer identifier must not exceed 100 characters")
        String assessorOfficerIdentifier
) {
}
