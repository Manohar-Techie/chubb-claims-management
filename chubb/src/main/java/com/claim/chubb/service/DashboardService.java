package com.claim.chubb.service;

import com.claim.chubb.dto.CurrencyLiabilityResponse;
import com.claim.chubb.dto.DashboardSummaryResponse;
import com.claim.chubb.dto.OfficerWorkloadResponse;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final String UNASSIGNED_OFFICER = "(Unassigned)";

    private final ClaimRepository claimRepository;

    public DashboardService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        Map<ClaimStatus, Long> claimsByStatus = new EnumMap<>(ClaimStatus.class);
        for (ClaimStatus status : ClaimStatus.values()) {
            claimsByStatus.put(status, 0L);
        }
        claimRepository.countClaimsGroupedByStatus().forEach(count ->
                claimsByStatus.put(count.getStatus(), count.getClaimCount()));

        List<OfficerWorkloadResponse> officerWorkload =
                claimRepository.countClaimsGroupedByOfficer().stream()
                        .map(count -> new OfficerWorkloadResponse(
                                count.getOfficerIdentifier() == null
                                        ? UNASSIGNED_OFFICER
                                        : count.getOfficerIdentifier(),
                                count.getClaimCount()))
                        .toList();

        List<CurrencyLiabilityResponse> liabilities =
                claimRepository.sumApprovedLiabilityGroupedByCurrency().stream()
                        .map(total -> new CurrencyLiabilityResponse(
                                total.getCurrency(), total.getOutstandingLiability()))
                        .toList();

        return new DashboardSummaryResponse(
                claimRepository.count(),
                Map.copyOf(claimsByStatus),
                officerWorkload,
                liabilities);
    }
}
