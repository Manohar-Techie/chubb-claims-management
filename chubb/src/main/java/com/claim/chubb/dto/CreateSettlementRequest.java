package com.claim.chubb.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateSettlementRequest(
        @NotNull(message = "Settlement amount is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Settlement amount must be greater than zero")
        @Digits(integer = 13, fraction = 2,
                message = "Settlement amount must have at most 13 integer and 2 fractional digits")
        BigDecimal settlementAmount,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be three uppercase letters")
        String currency,

        @NotBlank(message = "Payment reference is required")
        @Size(max = 100, message = "Payment reference must not exceed 100 characters")
        String paymentReference,

        @NotBlank(message = "Processed-by officer identifier is required")
        @Size(max = 100, message = "Processed-by officer identifier must not exceed 100 characters")
        String processedByOfficerIdentifier
) {
}
