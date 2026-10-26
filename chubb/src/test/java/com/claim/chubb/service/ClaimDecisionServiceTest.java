package com.claim.chubb.service;

import com.claim.chubb.dto.ClaimDecisionResponse;
import com.claim.chubb.dto.CreateClaimDecisionRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimDecision;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.enums.DecisionValue;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimDecisionRepository;
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
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimDecisionServiceTest {

    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimDecisionRepository decisionRepository;

    @InjectMocks
    private ClaimDecisionService decisionService;

    private Claim claim;
    private String claimReference;

    @BeforeEach
    void setUp() {
        claim = new Claim();
        claim.setClaimantIdentifier("claimant-1");
        claim.setClaimType("MOTOR");
        claim.setPolicyReference("policy-1");
        claim.setEstimatedLoss(new java.math.BigDecimal("500.00"));
        claim.setCurrency("USD");
        claim.setStatus(ClaimStatus.ASSESSED);
        claim.onCreate();
        claimReference = claim.getClaimReference();
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void successfullyApprovesClaim() {
        stubClaim();
        when(decisionRepository.save(any(ClaimDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimDecisionResponse response =
                decisionService.makeDecision(claimReference, request(DecisionValue.APPROVE, "Approved"));

        assertEquals(DecisionValue.APPROVE, response.decision());
        assertEquals(claimReference, response.claimReference());
        assertEquals("Approved", response.decisionReason());
        assertNotNull(response.decidedAt());
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void successfullyRejectsClaimAndStoresReason() {
        stubClaim();
        when(decisionRepository.save(any(ClaimDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimDecisionResponse response =
                decisionService.makeDecision(claimReference, request(DecisionValue.REJECT, "Insufficient evidence"));

        assertEquals(DecisionValue.REJECT, response.decision());
        assertEquals("Insufficient evidence", response.decisionReason());
        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        verify(decisionRepository).save(any(ClaimDecision.class));
    }

    @Test
    void rejectsDecisionWithoutReasonDuringValidation() {
        CreateClaimDecisionRequest request =
                new CreateClaimDecisionRequest(DecisionValue.REJECT, "officer-1", "  ");

        assertTrue(VALIDATOR.validate(request).stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("decisionReason")));
    }

    @Test
    void decisionOnNonexistentClaimFails() {
        when(claimRepository.findByClaimReference("missing"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                decisionService.makeDecision("missing",
                        request(DecisionValue.APPROVE, "Approved")));

        verifyNoInteractions(decisionRepository);
    }

    @ParameterizedTest
    @EnumSource(value = ClaimStatus.class, names = {
            "SUBMITTED", "UNDER_REVIEW", "INFORMATION_REQUIRED",
            "APPROVED", "REJECTED", "SETTLED"
    })
    void decisionIsRejectedUnlessClaimIsAssessed(ClaimStatus status) {
        stubClaim();
        claim.setStatus(status);

        assertThrows(InvalidClaimStateException.class, () ->
                decisionService.makeDecision(claimReference,
                        request(DecisionValue.APPROVE, "Approved")));

        verifyNoInteractions(decisionRepository);
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void preventsDecidingClaimTwice() {
        stubClaim();
        when(decisionRepository.save(any(ClaimDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        decisionService.makeDecision(claimReference,
                request(DecisionValue.APPROVE, "Approved"));

        assertThrows(InvalidClaimStateException.class, () ->
                decisionService.makeDecision(claimReference,
                        request(DecisionValue.REJECT, "Rejected")));

        verify(decisionRepository, times(1)).save(any(ClaimDecision.class));
    }

    @Test
    void retrievesDecisionHistoryInRepositoryChronologicalOrder() {
        stubClaim();
        ClaimDecision earlier = new ClaimDecision(
                claim, DecisionValue.APPROVE, "officer-1", "Initial decision");
        ClaimDecision later = new ClaimDecision(
                claim, DecisionValue.REJECT, "officer-2", "Later decision");
        when(decisionRepository
                .findByClaimClaimReferenceOrderByDecidedAtAscIdAsc(claimReference))
                .thenReturn(List.of(earlier, later));

        List<ClaimDecisionResponse> history =
                decisionService.getDecisionHistory(claimReference);

        assertEquals(2, history.size());
        assertEquals(DecisionValue.APPROVE, history.get(0).decision());
        assertEquals(DecisionValue.REJECT, history.get(1).decision());
        verify(decisionRepository)
                .findByClaimClaimReferenceOrderByDecidedAtAscIdAsc(claimReference);
    }

    @Test
    void decisionWriteFailurePropagatesAndTransactionalMethodRollsBackRuntimeFailures() {
        stubClaim();
        when(decisionRepository.save(any(ClaimDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        IllegalStateException failure = new IllegalStateException("Claim update failed");
        doThrow(failure).when(claimRepository).save(claim);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                decisionService.makeDecision(claimReference,
                        request(DecisionValue.APPROVE, "Approved")));

        assertSame(failure, thrown);
        verify(decisionRepository).save(any(ClaimDecision.class));
        var transactionAttribute = new AnnotationTransactionAttributeSource()
                .getTransactionAttribute(
                        getMakeDecisionMethod(), ClaimDecisionService.class);
        assertNotNull(transactionAttribute);
        assertTrue(transactionAttribute.rollbackOn(failure));
    }

    private CreateClaimDecisionRequest request(DecisionValue value, String reason) {
        return new CreateClaimDecisionRequest(value, "officer-1", reason);
    }

    private void stubClaim() {
        when(claimRepository.findByClaimReference(claimReference))
                .thenReturn(Optional.of(claim));
    }

    private java.lang.reflect.Method getMakeDecisionMethod() {
        try {
            return ClaimDecisionService.class.getMethod(
                    "makeDecision", String.class, CreateClaimDecisionRequest.class);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError(exception);
        }
    }
}
