package com.claim.chubb.controller;

import com.claim.chubb.dto.ClaimResponse;
import com.claim.chubb.dto.CreateClaimRequest;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.claim.chubb.dto.AssignClaimRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimAssignment;
import com.claim.chubb.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final AssignmentService assignmentService;

    public ClaimController(ClaimService claimService, AssignmentService assignmentService) {
        this.claimService = claimService;
        this.assignmentService = assignmentService;
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
    @PutMapping("/{claimReference}/assignment")
    public ResponseEntity<Map<String, Object>> assignClaim(
            @PathVariable String claimReference,
            @Valid @RequestBody AssignClaimRequest request) {

        Claim claim = assignmentService.assignClaim(
                claimReference, request);

        return ResponseEntity.ok(Map.of(
                "claimReference", claim.getClaimReference(),
                "assignedOfficerIdentifier",
                claim.getAssignedOfficerIdentifier(),
                "status", claim.getStatus()
        ));
    }

    @GetMapping("/{claimReference}/assignments")
    public ResponseEntity<List<Map<String, Object>>> getAssignmentHistory(
            @PathVariable String claimReference) {

        List<Map<String, Object>> history =
                assignmentService.getAssignmentHistory(claimReference)
                        .stream()
                        .map(assignment -> Map.<String, Object>of(
                                "officerIdentifier",
                                assignment.getOfficerIdentifier(),
                                "assignedAt",
                                assignment.getAssignedAt()
                        ))
                        .toList();

        return ResponseEntity.ok(history);
    }
}