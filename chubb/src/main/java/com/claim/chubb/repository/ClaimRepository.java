package com.claim.chubb.repository;

import com.claim.chubb.entity.Claim;
import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.enums.ClaimType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimReference(String claimReference);

    Page<Claim> findByClaimantIdentifier(String claimantIdentifier, Pageable pageable);

    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);

    Page<Claim> findByAssignedOfficerIdentifier(String assignedOfficerIdentifier, Pageable pageable);

    @Query(
            value = """
                    SELECT claim
                    FROM Claim claim
                    WHERE (:claimantIdentifier IS NULL OR claim.claimantIdentifier = :claimantIdentifier)
                      AND (:marketCode IS NULL OR claim.marketCode = :marketCode)
                      AND (:status IS NULL OR claim.status = :status)
                      AND (:claimType IS NULL OR claim.claimType = :claimType)
                      AND (:assignedOfficerIdentifier IS NULL
                           OR claim.assignedOfficerIdentifier = :assignedOfficerIdentifier)
                      AND (:incidentDateFrom IS NULL OR claim.incidentDate >= :incidentDateFrom)
                      AND (:incidentDateTo IS NULL OR claim.incidentDate <= :incidentDateTo)
                    """,
            countQuery = """
                    SELECT COUNT(claim)
                    FROM Claim claim
                    WHERE (:claimantIdentifier IS NULL OR claim.claimantIdentifier = :claimantIdentifier)
                      AND (:marketCode IS NULL OR claim.marketCode = :marketCode)
                      AND (:status IS NULL OR claim.status = :status)
                      AND (:claimType IS NULL OR claim.claimType = :claimType)
                      AND (:assignedOfficerIdentifier IS NULL
                           OR claim.assignedOfficerIdentifier = :assignedOfficerIdentifier)
                      AND (:incidentDateFrom IS NULL OR claim.incidentDate >= :incidentDateFrom)
                      AND (:incidentDateTo IS NULL OR claim.incidentDate <= :incidentDateTo)
                    """
    )
    Page<Claim> searchClaims(
            @Param("claimantIdentifier") String claimantIdentifier,
            @Param("marketCode") String marketCode,
            @Param("status") ClaimStatus status,
            @Param("claimType") ClaimType claimType,
            @Param("assignedOfficerIdentifier") String assignedOfficerIdentifier,
            @Param("incidentDateFrom") LocalDate incidentDateFrom,
            @Param("incidentDateTo") LocalDate incidentDateTo,
            Pageable pageable
    );
}
