package com.claim.chubb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProvideInformationRequest(
        @NotBlank(message = "Response is required")
        @Size(max = 4000, message = "Response must not exceed 4000 characters")
        String response
) {
}
