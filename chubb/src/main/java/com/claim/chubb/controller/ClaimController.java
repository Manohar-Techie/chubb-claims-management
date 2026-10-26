package com.claim.chubb.controller;

import com.claim.chubb.dto.ClaimInformationRequestResponse;
import com.claim.chubb.dto.ClaimDecisionResponse;
import com.claim.chubb.dto.ClaimResponse;
import com.claim.chubb.dto.CreateClaimDecisionRequest;
import com.claim.chubb.dto.CreateClaimRequest;
import com.claim.chubb.dto.ProvideInformationRequest;
import com.claim.chubb.dto.RequestInformationRequest;
import com.claim.chubb.dto.AssignClaimRequest;
import com.claim.chubb.dto.AssessmentResponse;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.dto.CreateAssessmentRequest;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.service.AssignmentService;
import com.claim.chubb.service.AssessmentService;
import com.claim.chubb.service.ClaimService;
import com.claim.chubb.service.ClaimDecisionService;
import com.claim.chubb.service.InformationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final AssignmentService assignmentService;
    private final InformationService informationService;
    private final AssessmentService assessmentService;
    private final ClaimDecisionService claimDecisionService;

    public ClaimController(
            ClaimService claimService,
            AssignmentService assignmentService,
            InformationService informationService,
            AssessmentService assessmentService,
            ClaimDecisionService claimDecisionService) {
        this.claimService = claimService;
        this.assignmentService = assignmentService;
        this.informationService = informationService;
        this.assessmentService = assessmentService;
        this.claimDecisionService = claimDecisionService;
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

    @PostMapping("/{claimReference}/information-requests")
    public ResponseEntity<ClaimInformationRequestResponse> requestInformation(
            @PathVariable String claimReference,
            @Valid @RequestBody RequestInformationRequest request) {

        ClaimInformationRequestResponse response =
                informationService.requestInformation(claimReference, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{claimReference}/information-requests")
    public ResponseEntity<List<ClaimInformationRequestResponse>> getInformationRequests(
            @PathVariable String claimReference) {

        return ResponseEntity.ok(
                informationService.getInformationRequests(claimReference));
    }

    @PostMapping("/{claimReference}/information-requests/{requestId}/response")
    public ResponseEntity<ClaimInformationRequestResponse> provideInformation(
            @PathVariable String claimReference,
            @PathVariable Long requestId,
            @Valid @RequestBody ProvideInformationRequest request) {

        return ResponseEntity.ok(
                informationService.provideInformation(claimReference, requestId, request));
    }

    @PostMapping("/{claimReference}/assessments")
    public ResponseEntity<AssessmentResponse> assessClaim(
            @PathVariable String claimReference,
            @Valid @RequestBody CreateAssessmentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assessmentService.assessClaim(claimReference, request));
    }

    @GetMapping("/{claimReference}/assessments")
    public ResponseEntity<List<AssessmentResponse>> getAssessmentHistory(
            @PathVariable String claimReference) {

        return ResponseEntity.ok(
                assessmentService.getAssessmentHistory(claimReference));
    }

    @PostMapping("/{claimReference}/decision")
    public ResponseEntity<ClaimDecisionResponse> decideClaim(
            @PathVariable String claimReference,
            @Valid @RequestBody CreateClaimDecisionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimDecisionService.makeDecision(claimReference, request));
    }

    @GetMapping("/{claimReference}/decisions")
    public ResponseEntity<List<ClaimDecisionResponse>> getDecisionHistory(
            @PathVariable String claimReference) {

        return ResponseEntity.ok(
                claimDecisionService.getDecisionHistory(claimReference));
    }
}