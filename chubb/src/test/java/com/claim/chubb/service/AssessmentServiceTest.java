package com.claim.chubb.service;

import com.claim.chubb.dto.AssessmentResponse;
import com.claim.chubb.dto.CreateAssessmentRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimAssessment;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimAssessmentRepository;
import com.claim.chubb.repository.ClaimRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimAssessmentRepository assessmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AssessmentService assessmentService;

    private Claim claim;
    private String claimReference;

    @BeforeEach
    void setUp() {
        claim = new Claim();
        claim.setClaimantIdentifier("claimant-1");
        claim.setClaimType("MOTOR");
        claim.setPolicyReference("policy-1");
        claim.setEstimatedLoss(new BigDecimal("500.00"));
        claim.setCurrency("USD");
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.onCreate();
        claimReference = claim.getClaimReference();
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void assessesClaimAndUpdatesClaimWithNewAssessmentRecord() {
        stubClaim();
        when(assessmentRepository.save(any(ClaimAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponse response =
                assessmentService.assessClaim(claimReference, validRequest());

        assertEquals(claimReference, response.claimReference());
        assertEquals(new BigDecimal("250.00"), response.assessedLiability());
        assertEquals("USD", response.currency());
        assertEquals("Assessment rationale", response.rationale());
        assertEquals("officer-1", response.assessorOfficerIdentifier());
        assertNotNull(response.assessedAt());
        assertEquals(new BigDecimal("250.00"), claim.getAssessedLiability());
        assertEquals(ClaimStatus.ASSESSED, claim.getStatus());
        verify(assessmentRepository).save(any(ClaimAssessment.class));
        verify(claimRepository).save(claim);
        verify(auditService).recordEvent(
                eq(claimReference), eq("CLAIM_ASSESSED"), eq("officer-1"),
                eq(ClaimStatus.UNDER_REVIEW), eq(ClaimStatus.ASSESSED),
                contains("250.00"));
    }

    @Test
    void returnsAssessmentHistoryFromChronologicalRepositoryQuery() {
        stubClaim();
        AssessmentResponse older = new AssessmentResponse(
                1L, claimReference, new BigDecimal("100.00"), "USD", "First",
                "officer-1", java.time.LocalDateTime.parse("2026-01-01T10:00:00"));
        AssessmentResponse later = new AssessmentResponse(
                2L, claimReference, new BigDecimal("200.00"), "USD", "Second",
                "officer-2", java.time.LocalDateTime.parse("2026-01-02T10:00:00"));
        ClaimAssessment first = new ClaimAssessment(claim, older.assessedLiability(),
                older.currency(), older.rationale(), older.assessorOfficerIdentifier());
        ClaimAssessment second = new ClaimAssessment(claim, later.assessedLiability(),
                later.currency(), later.rationale(), later.assessorOfficerIdentifier());
        when(assessmentRepository
                .findByClaimClaimReferenceOrderByAssessedAtAscIdAsc(claimReference))
                .thenReturn(List.of(first, second));

        List<AssessmentResponse> history =
                assessmentService.getAssessmentHistory(claimReference);

        assertEquals(2, history.size());
        assertEquals(new BigDecimal("100.00"), history.get(0).assessedLiability());
        assertEquals(new BigDecimal("200.00"), history.get(1).assessedLiability());
        verify(assessmentRepository)
                .findByClaimClaimReferenceOrderByAssessedAtAscIdAsc(claimReference);
    }

    @Test
    void rejectsNegativeLiabilityAndInvalidCurrencyDuringValidation() {
        CreateAssessmentRequest negativeLiability = new CreateAssessmentRequest(
                new BigDecimal("-0.01"), "USD", "Rationale", "officer-1");
        CreateAssessmentRequest invalidCurrency = new CreateAssessmentRequest(
                BigDecimal.ZERO, "usd", "Rationale", "officer-1");

        assertTrue(VALIDATOR.validate(negativeLiability).stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("assessedLiability")));
        assertTrue(VALIDATOR.validate(invalidCurrency).stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void assessmentOfNonexistentClaimFails() {
        when(claimRepository.findByClaimReference("missing"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                assessmentService.assessClaim("missing", validRequest()));

        verifyNoInteractions(assessmentRepository);
    }

    @ParameterizedTest
    @EnumSource(value = ClaimStatus.class, names = {
            "SUBMITTED", "INFORMATION_REQUIRED", "ASSESSED", "APPROVED", "REJECTED", "SETTLED"
    })
    void assessmentIsRejectedForClaimInInvalidStatus(ClaimStatus status) {
        stubClaim();
        claim.setStatus(status);

        assertThrows(InvalidClaimStateException.class, () ->
                assessmentService.assessClaim(claimReference, validRequest()));

        verifyNoInteractions(assessmentRepository);
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void keepsPreviousAssessmentsWhenClaimIsAssessedAgainAfterReturningToReview() {
        stubClaim();
        when(assessmentRepository.save(any(ClaimAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assessmentService.assessClaim(claimReference, validRequest());
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        assessmentService.assessClaim(claimReference, new CreateAssessmentRequest(
                new BigDecimal("300.00"), "USD", "Updated rationale", "officer-2"));

        verify(assessmentRepository, times(2)).save(any(ClaimAssessment.class));
    }

    private CreateAssessmentRequest validRequest() {
        return new CreateAssessmentRequest(
                new BigDecimal("250.00"), "USD", "Assessment rationale", "officer-1");
    }

    private void stubClaim() {
        when(claimRepository.findByClaimReference(claimReference))
                .thenReturn(Optional.of(claim));
    }
}
