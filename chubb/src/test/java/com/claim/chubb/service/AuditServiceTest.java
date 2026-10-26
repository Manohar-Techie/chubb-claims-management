package com.claim.chubb.service;

import com.claim.chubb.dto.AuditLogResponse;
import com.claim.chubb.entity.AuditLog;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.exception.ResourceNotFoundException;
import com.claim.chubb.repository.AuditLogRepository;
import com.claim.chubb.repository.ClaimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void recordsAuditEventWithStatusesAndDetails() {
        auditService.recordEvent(
                "CLM-123", "CLAIM_SUBMITTED", "claimant-1",
                null, ClaimStatus.SUBMITTED, "Claim submitted");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertEquals("CLM-123", saved.getClaimReference());
        assertEquals("CLAIM_SUBMITTED", saved.getAction());
        assertEquals("claimant-1", saved.getPerformedByIdentifier());
        assertNull(saved.getPreviousStatus());
        assertEquals(ClaimStatus.SUBMITTED, saved.getNewStatus());
        assertEquals("Claim submitted", saved.getDetails());
        assertNotNull(saved.getOccurredAt());
    }

    @Test
    void retrievesAuditHistoryInChronologicalRepositoryOrder() {
        when(claimRepository.existsByClaimReference("CLM-123")).thenReturn(true);
        AuditLog earlier = new AuditLog(
                "CLM-123", "CLAIM_SUBMITTED", "claimant-1",
                null, ClaimStatus.SUBMITTED, "Submitted");
        AuditLog later = new AuditLog(
                "CLM-123", "CLAIM_ASSIGNED", "officer-1",
                ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW, "Assigned");
        when(auditLogRepository
                .findByClaimReferenceOrderByOccurredAtAscIdAsc("CLM-123"))
                .thenReturn(List.of(earlier, later));

        List<AuditLogResponse> history = auditService.getAuditHistory("CLM-123");

        assertEquals(2, history.size());
        assertEquals("CLAIM_SUBMITTED", history.getFirst().action());
        assertEquals("CLAIM_ASSIGNED", history.get(1).action());
        verify(auditLogRepository)
                .findByClaimReferenceOrderByOccurredAtAscIdAsc("CLM-123");
    }

    @Test
    void missingClaimThrowsNotFound() {
        when(claimRepository.existsByClaimReference("missing")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> auditService.getAuditHistory("missing"));
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void auditWriteIsTransactionalAndRuntimeFailuresRollBack() throws Exception {
        var transactionAttribute = new AnnotationTransactionAttributeSource()
                .getTransactionAttribute(
                        AuditService.class.getMethod(
                                "recordEvent", String.class, String.class, String.class,
                                ClaimStatus.class, ClaimStatus.class, String.class),
                        AuditService.class);

        assertNotNull(transactionAttribute);
        assertTrue(transactionAttribute.rollbackOn(new IllegalStateException()));
    }
}
