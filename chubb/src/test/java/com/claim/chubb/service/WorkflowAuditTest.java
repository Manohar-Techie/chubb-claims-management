package com.claim.chubb.service;

import com.claim.chubb.dto.AssignClaimRequest;
import com.claim.chubb.dto.CreateClaimRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimAssignment;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.repository.ClaimAssignmentRepository;
import com.claim.chubb.repository.ClaimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowAuditTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimAssignmentRepository assignmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ClaimService claimService;

    @InjectMocks
    private AssignmentService assignmentService;

    @Test
    void claimSubmissionRecordsClaimantAsActor() {
        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> {
                    Claim claim = invocation.getArgument(0);
                    claim.onCreate();
                    return claim;
                });

        claimService.submitClaim(new CreateClaimRequest(
                "claimant-1", "MOTOR", "policy-1", LocalDate.now(),
                "Auckland", "Vehicle damage", new BigDecimal("500.00"), "USD"));

        verify(auditService).recordEvent(
                startsWith("CLM-"), eq("CLAIM_SUBMITTED"), eq("claimant-1"),
                isNull(), eq(ClaimStatus.SUBMITTED),
                contains("policy-1"));
    }

    @Test
    void assignmentRecordsOfficerAndActualStatusTransition() {
        Claim claim = new Claim();
        claim.onCreate();
        claim.setStatus(ClaimStatus.SUBMITTED);
        when(claimRepository.findByClaimReference(claim.getClaimReference()))
                .thenReturn(Optional.of(claim));
        when(claimRepository.save(claim)).thenReturn(claim);
        when(assignmentRepository.save(any(ClaimAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assignmentService.assignClaim(
                claim.getClaimReference(), new AssignClaimRequest(" officer-1 "));

        verify(auditService).recordEvent(
                eq(claim.getClaimReference()), eq("CLAIM_ASSIGNED"), eq("officer-1"),
                eq(ClaimStatus.SUBMITTED), eq(ClaimStatus.UNDER_REVIEW),
                contains("officer-1"));
    }
}
