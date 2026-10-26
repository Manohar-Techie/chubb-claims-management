package com.claim.chubb.repository;

import com.claim.chubb.entity.Claim;
import com.claim.chubb.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimReference(String claimReference);

    List<Claim> findByClaimantIdentifier(
            String claimantIdentifier);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByAssignedOfficerIdentifier(
            String assignedOfficerIdentifier);

    boolean existsByClaimReference(String claimReference);

    @Query("select c.status as status, count(c.id) as claimCount from Claim c group by c.status")
    List<StatusCountProjection> countClaimsGroupedByStatus();

    @Query("select c.assignedOfficerIdentifier as officerIdentifier, count(c.id) as claimCount "
            + "from Claim c group by c.assignedOfficerIdentifier order by c.assignedOfficerIdentifier")
    List<OfficerClaimCountProjection> countClaimsGroupedByOfficer();

    @Query("select c.currency as currency, sum(c.assessedLiability) as outstandingLiability "
            + "from Claim c where c.status = com.claim.chubb.enums.ClaimStatus.APPROVED "
            + "and c.assessedLiability is not null group by c.currency order by c.currency")
    List<CurrencyLiabilityProjection> sumApprovedLiabilityGroupedByCurrency();

    interface StatusCountProjection {
        ClaimStatus getStatus();
        Long getClaimCount();
    }

    interface OfficerClaimCountProjection {
        String getOfficerIdentifier();
        Long getClaimCount();
    }

    interface CurrencyLiabilityProjection {
        String getCurrency();
        BigDecimal getOutstandingLiability();
    }
}