package com.claim.chubb.service;

import com.claim.chubb.dto.CreateSettlementRequest;
import com.claim.chubb.dto.SettlementResponse;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimSettlement;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.ClaimRepository;
import com.claim.chubb.repository.ClaimSettlementRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimSettlementRepository settlementRepository;

    @InjectMocks
    private SettlementService settlementService;

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
        claim.setAssessedLiability(new BigDecimal("250.00"));
        claim.setStatus(ClaimStatus.APPROVED);
        claim.onCreate();
        claimReference = claim.getClaimReference();
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void successfullySettlesApprovedClaim() {
        stubApprovedClaim();
        when(settlementRepository.existsByClaimClaimReference(claimReference))
                .thenReturn(false);
        when(settlementRepository.existsByPaymentReference("PAY-001"))
                .thenReturn(false);
        when(settlementRepository.saveAndFlush(any(ClaimSettlement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SettlementResponse response =
                settlementService.settleClaim(claimReference, validRequest());

        assertEquals(claimReference, response.claimReference());
        assertEquals(new BigDecimal("250.00"), response.settlementAmount());
        assertEquals("USD", response.currency());
        assertEquals("PAY-001", response.paymentReference());
        assertEquals("officer-1", response.processedByOfficerIdentifier());
        assertNotNull(response.settledAt());
        assertEquals(ClaimStatus.SETTLED, claim.getStatus());
        verify(settlementRepository).saveAndFlush(any(ClaimSettlement.class));
        verify(claimRepository).save(claim);
    }

    @Test
    void settlementOfNonexistentClaimFails() {
        when(claimRepository.findByClaimReference("missing"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                settlementService.settleClaim("missing", validRequest()));

        verifyNoInteractions(settlementRepository);
    }

    @Test
    void settlementOfUnapprovedClaimFails() {
        stubApprovedClaim();
        claim.setStatus(ClaimStatus.ASSESSED);

        assertThrows(InvalidClaimStateException.class, () ->
                settlementService.settleClaim(claimReference, validRequest()));

        verifyNoInteractions(settlementRepository);
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.00", "0.00"})
    void rejectsNegativeAndZeroSettlementAmountsDuringValidation(String amount) {
        CreateSettlementRequest request = new CreateSettlementRequest(
                new BigDecimal(amount), "USD", "PAY-001", "officer-1");

        assertTrue(VALIDATOR.validate(request).stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("settlementAmount")));
    }

    @Test
    void rejectsInvalidCurrencyDuringValidation() {
        CreateSettlementRequest request = new CreateSettlementRequest(
                new BigDecimal("250.00"), "usd", "PAY-001", "officer-1");

        assertTrue(VALIDATOR.validate(request).stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("currency")));
    }

    @Test
    void rejectsCurrencyMismatch() {
        stubApprovedClaim();
        when(settlementRepository.existsByClaimClaimReference(claimReference))
                .thenReturn(false);
        when(settlementRepository.existsByPaymentReference("PAY-001"))
                .thenReturn(false);
        CreateSettlementRequest request = new CreateSettlementRequest(
                new BigDecimal("250.00"), "CAD", "PAY-001", "officer-1");

        assertThrows(InvalidClaimStateException.class, () ->
                settlementService.settleClaim(claimReference, request));

        verify(settlementRepository, never()).saveAndFlush(any(ClaimSettlement.class));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void rejectsAmountDifferentFromAssessedLiability() {
        stubApprovedClaim();
        when(settlementRepository.existsByClaimClaimReference(claimReference))
                .thenReturn(false);
        when(settlementRepository.existsByPaymentReference("PAY-001"))
                .thenReturn(false);
        CreateSettlementRequest request = new CreateSettlementRequest(
                new BigDecimal("249.99"), "USD", "PAY-001", "officer-1");

        assertThrows(InvalidClaimStateException.class, () ->
                settlementService.settleClaim(claimReference, request));

        verify(settlementRepository, never()).saveAndFlush(any(ClaimSettlement.class));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void rejectsDuplicatePaymentReference() {
        stubApprovedClaim();
        when(settlementRepository.existsByClaimClaimReference(claimReference))
                .thenReturn(false);
        when(settlementRepository.existsByPaymentReference("PAY-001"))
                .thenReturn(true);

        assertThrows(InvalidClaimStateException.class, () ->
                settlementService.settleClaim(claimReference, validRequest()));

        verify(settlementRepository, never()).saveAndFlush(any(ClaimSettlement.class));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void rejectsSecondSettlementForClaim() {
        stubApprovedClaim();
        when(settlementRepository.existsByClaimClaimReference(claimReference))
                .thenReturn(true);

        assertThrows(InvalidClaimStateException.class, () ->
                settlementService.settleClaim(claimReference, validRequest()));

        verify(settlementRepository, never()).saveAndFlush(any(ClaimSettlement.class));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void retrievesSettlementHistory() {
        stubApprovedClaim();
        ClaimSettlement settlement = new ClaimSettlement(
                claim, new BigDecimal("250.00"), "USD", "PAY-001", "officer-1");
        when(settlementRepository
                .findByClaimClaimReferenceOrderBySettledAtAscIdAsc(claimReference))
                .thenReturn(List.of(settlement));

        List<SettlementResponse> settlements =
                settlementService.getSettlements(claimReference);

        assertEquals(1, settlements.size());
        assertEquals("PAY-001", settlements.getFirst().paymentReference());
        verify(settlementRepository)
                .findByClaimClaimReferenceOrderBySettledAtAscIdAsc(claimReference);
    }

    @Test
    void settlementPersistenceFailureDoesNotUpdateClaim() {
        stubApprovedClaim();
        when(settlementRepository.existsByClaimClaimReference(claimReference))
                .thenReturn(false);
        when(settlementRepository.existsByPaymentReference("PAY-001"))
                .thenReturn(false);
        when(settlementRepository.saveAndFlush(any(ClaimSettlement.class)))
                .thenThrow(new IllegalStateException("Persistence failed"));

        assertThrows(IllegalStateException.class, () ->
                settlementService.settleClaim(claimReference, validRequest()));

        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void transactionRollsBackRuntimeFailureAfterClaimUpdate() {
        var transactionAttribute = new AnnotationTransactionAttributeSource()
                .getTransactionAttribute(
                        getSettleClaimMethod(), SettlementService.class);

        assertNotNull(transactionAttribute);
        assertTrue(transactionAttribute.rollbackOn(new IllegalStateException()));
    }

    private void stubApprovedClaim() {
        when(claimRepository.findByClaimReference(claimReference))
                .thenReturn(Optional.of(claim));
    }

    private CreateSettlementRequest validRequest() {
        return new CreateSettlementRequest(
                new BigDecimal("250.00"), "USD", "PAY-001", "officer-1");
    }

    private java.lang.reflect.Method getSettleClaimMethod() {
        try {
            return SettlementService.class.getMethod(
                    "settleClaim", String.class, CreateSettlementRequest.class);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError(exception);
        }
    }
}
