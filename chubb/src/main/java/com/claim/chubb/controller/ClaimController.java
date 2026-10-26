package com.claim.chubb.controller;

import com.claim.chubb.dto.ClaimResponse;
import com.claim.chubb.dto.CreateClaimRequest;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse submitClaim(
            @Valid @RequestBody CreateClaimRequest request) {

        return claimService.submitClaim(request);
    }

    @GetMapping("/{claimReference}")
    public ClaimResponse getClaim(
            @PathVariable String claimReference) {

        return claimService.getClaim(claimReference);
    }

    @GetMapping
    public List<ClaimResponse> getClaims(
            @RequestParam(required = false) ClaimStatus status) {

        if (status != null) {
            return claimService.getClaimsByStatus(status);
        }

        return claimService.getAllClaims();
    }
}