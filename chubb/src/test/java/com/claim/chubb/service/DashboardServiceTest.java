package com.claim.chubb.service;

import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.repository.ClaimRepository;
import com.claim.chubb.repository.ClaimRepository.CurrencyLiabilityProjection;
import com.claim.chubb.repository.ClaimRepository.OfficerClaimCountProjection;
import com.claim.chubb.repository.ClaimRepository.StatusCountProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void emptyDataReturnsZeroCountsForEveryStatusAndEmptySummaries() {
        when(claimRepository.count()).thenReturn(0L);
        when(claimRepository.countClaimsGroupedByStatus()).thenReturn(List.of());
        when(claimRepository.countClaimsGroupedByOfficer()).thenReturn(List.of());
        when(claimRepository.sumApprovedLiabilityGroupedByCurrency()).thenReturn(List.of());

        var summary = dashboardService.getSummary();

        assertEquals(0L, summary.totalClaims());
        assertEquals(ClaimStatus.values().length, summary.claimsByStatus().size());
        for (ClaimStatus status : ClaimStatus.values()) {
            assertEquals(0L, summary.claimsByStatus().get(status));
        }
        assertTrue(summary.officerWorkload().isEmpty());
        assertTrue(summary.outstandingLiabilityByCurrency().isEmpty());
    }

    @Test
    void returnsStatusCountsAndIncludesStatusesWithNoClaims() {
        StatusCountProjection submitted = mock(StatusCountProjection.class);
        when(submitted.getStatus()).thenReturn(ClaimStatus.SUBMITTED);
        when(submitted.getClaimCount()).thenReturn(4L);
        StatusCountProjection approved = mock(StatusCountProjection.class);
        when(approved.getStatus()).thenReturn(ClaimStatus.APPROVED);
        when(approved.getClaimCount()).thenReturn(2L);
        when(claimRepository.count()).thenReturn(6L);
        when(claimRepository.countClaimsGroupedByStatus())
                .thenReturn(List.of(submitted, approved));
        when(claimRepository.countClaimsGroupedByOfficer()).thenReturn(List.of());
        when(claimRepository.sumApprovedLiabilityGroupedByCurrency()).thenReturn(List.of());

        var summary = dashboardService.getSummary();

        assertEquals(6L, summary.totalClaims());
        assertEquals(4L, summary.claimsByStatus().get(ClaimStatus.SUBMITTED));
        assertEquals(2L, summary.claimsByStatus().get(ClaimStatus.APPROVED));
        assertEquals(0L, summary.claimsByStatus().get(ClaimStatus.SETTLED));
    }

    @Test
    void returnsOfficerWorkloadAndExplicitUnassignedGroup() {
        OfficerClaimCountProjection assigned = mock(OfficerClaimCountProjection.class);
        when(assigned.getOfficerIdentifier()).thenReturn("officer-1");
        when(assigned.getClaimCount()).thenReturn(3L);
        OfficerClaimCountProjection unassigned = mock(OfficerClaimCountProjection.class);
        when(unassigned.getOfficerIdentifier()).thenReturn(null);
        when(unassigned.getClaimCount()).thenReturn(2L);
        when(claimRepository.count()).thenReturn(5L);
        when(claimRepository.countClaimsGroupedByStatus()).thenReturn(List.of());
        when(claimRepository.countClaimsGroupedByOfficer())
                .thenReturn(List.of(assigned, unassigned));
        when(claimRepository.sumApprovedLiabilityGroupedByCurrency()).thenReturn(List.of());

        var workload = dashboardService.getSummary().officerWorkload();

        assertEquals(2, workload.size());
        assertEquals("officer-1", workload.getFirst().officerIdentifier());
        assertEquals(3L, workload.getFirst().claimCount());
        assertEquals("(Unassigned)", workload.get(1).officerIdentifier());
        assertEquals(2L, workload.get(1).claimCount());
    }

    @Test
    void returnsOutstandingApprovedLiabilitySeparatedByCurrency() {
        CurrencyLiabilityProjection usd = mock(CurrencyLiabilityProjection.class);
        when(usd.getCurrency()).thenReturn("USD");
        when(usd.getOutstandingLiability()).thenReturn(new BigDecimal("100.00"));
        CurrencyLiabilityProjection cad = mock(CurrencyLiabilityProjection.class);
        when(cad.getCurrency()).thenReturn("CAD");
        when(cad.getOutstandingLiability()).thenReturn(new BigDecimal("250.00"));
        when(claimRepository.count()).thenReturn(2L);
        when(claimRepository.countClaimsGroupedByStatus()).thenReturn(List.of());
        when(claimRepository.countClaimsGroupedByOfficer()).thenReturn(List.of());
        when(claimRepository.sumApprovedLiabilityGroupedByCurrency())
                .thenReturn(List.of(cad, usd));

        var liabilities = dashboardService.getSummary().outstandingLiabilityByCurrency();

        assertEquals(2, liabilities.size());
        assertEquals("CAD", liabilities.getFirst().currency());
        assertEquals(new BigDecimal("250.00"), liabilities.getFirst().outstandingLiability());
        assertEquals("USD", liabilities.get(1).currency());
        assertEquals(new BigDecimal("100.00"), liabilities.get(1).outstandingLiability());
        verify(claimRepository).sumApprovedLiabilityGroupedByCurrency();
    }
}
