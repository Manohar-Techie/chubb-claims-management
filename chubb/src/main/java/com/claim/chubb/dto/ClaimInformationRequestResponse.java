package com.claim.chubb.dto;

import java.time.LocalDateTime;

public record ClaimInformationRequestResponse(
        Long id,
        String description,
        LocalDateTime requestedAt,
        String response,
        LocalDateTime responseAt
) {
}
