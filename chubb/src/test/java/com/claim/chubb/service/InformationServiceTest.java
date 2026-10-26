package com.claim.chubb.service;

import com.claim.chubb.dto.ProvideInformationRequest;
import com.claim.chubb.dto.RequestInformationRequest;
import com.claim.chubb.entity.Claim;
import com.claim.chubb.entity.ClaimInformationRequest;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.InvalidClaimStateException;
import com.claim.chubb.repository.ClaimInformationRequestRepository;
import com.claim.chubb.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InformationServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimInformationRequestRepository informationRequestRepository;

    @InjectMocks
    private InformationService informationService;

    private Claim claim;
    private String claimReference;

    @BeforeEach
    void setUp() {
        claim = new Claim();
        claim.setClaimantIdentifier("claimant-1");
        claim.setClaimType("MOTOR");
        claim.setPolicyReference("policy-1");
        claim.setEstimatedLoss(java.math.BigDecimal.valueOf(100));
        claim.setCurrency("USD");
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.onCreate();
        claimReference = claim.getClaimReference();

        when(claimRepository.findByClaimReference(claimReference))
                .thenReturn(Optional.of(claim));
    }

    @Test
    void requestInformationCreatesRequestAndMovesClaimToInformationRequired() {
        when(informationRequestRepository.save(any(ClaimInformationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = informationService.requestInformation(
                claimReference, new RequestInformationRequest("  Provide repair records  "));

        assertEquals("Provide repair records", result.description());
        assertNotNull(result.requestedAt());
        assertEquals(ClaimStatus.INFORMATION_REQUIRED, claim.getStatus());
        verify(informationRequestRepository).save(any(ClaimInformationRequest.class));
        verify(claimRepository).save(claim);
    }

    @Test
    void provideInformationReturnsClaimToReviewWhenAllRequestsAreAnswered() {
        ClaimInformationRequest informationRequest =
                new ClaimInformationRequest(claim, "Provide repair records");
        when(informationRequestRepository.findById(12L))
                .thenReturn(Optional.of(informationRequest));
        when(informationRequestRepository
                .countByClaimClaimReferenceAndResponseIsNull(claimReference))
                .thenReturn(0L);
        when(informationRequestRepository.save(any(ClaimInformationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        claim.setStatus(ClaimStatus.INFORMATION_REQUIRED);

        var result = informationService.provideInformation(
                claimReference, 12L, new ProvideInformationRequest("  Records attached  "));

        assertEquals("Records attached", result.response());
        assertNotNull(result.responseAt());
        assertEquals(ClaimStatus.UNDER_REVIEW, claim.getStatus());
        verify(claimRepository).save(claim);
    }

    @Test
    void provideInformationKeepsClaimWaitingWhenOtherRequestsRemainUnanswered() {
        ClaimInformationRequest informationRequest =
                new ClaimInformationRequest(claim, "Provide repair records");
        when(informationRequestRepository.findById(12L))
                .thenReturn(Optional.of(informationRequest));
        when(informationRequestRepository
                .countByClaimClaimReferenceAndResponseIsNull(claimReference))
                .thenReturn(1L);
        when(informationRequestRepository.save(any(ClaimInformationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        claim.setStatus(ClaimStatus.INFORMATION_REQUIRED);

        informationService.provideInformation(
                claimReference, 12L, new ProvideInformationRequest("Records attached"));

        assertEquals(ClaimStatus.INFORMATION_REQUIRED, claim.getStatus());
    }

    @Test
    void provideInformationRejectsDuplicateResponse() {
        ClaimInformationRequest informationRequest =
                new ClaimInformationRequest(claim, "Provide repair records");
        informationRequest.provideResponse("Already received");
        when(informationRequestRepository.findById(12L))
                .thenReturn(Optional.of(informationRequest));
        claim.setStatus(ClaimStatus.INFORMATION_REQUIRED);

        assertThrows(InvalidClaimStateException.class, () ->
                informationService.provideInformation(
                        claimReference, 12L, new ProvideInformationRequest("Another response")));

        verify(informationRequestRepository, never())
                .save(any(ClaimInformationRequest.class));
    }

    @ParameterizedTest
    @EnumSource(value = ClaimStatus.class, names = {"SUBMITTED", "APPROVED", "REJECTED", "SETTLED"})
    void requestInformationRejectsClaimsThatAreNotUnderReview(ClaimStatus status) {
        claim.setStatus(status);

        assertThrows(InvalidClaimStateException.class, () ->
                informationService.requestInformation(
                        claimReference, new RequestInformationRequest("Provide records")));

        verify(informationRequestRepository, never())
                .save(any(ClaimInformationRequest.class));
    }

    @Test
    void provideInformationRejectsClaimNotWaitingForInformation() {
        ClaimInformationRequest informationRequest =
                new ClaimInformationRequest(claim, "Provide repair records");
        when(informationRequestRepository.findById(12L))
                .thenReturn(Optional.of(informationRequest));

        assertThrows(InvalidClaimStateException.class, () ->
                informationService.provideInformation(
                        claimReference, 12L, new ProvideInformationRequest("Records attached")));
    }
}
