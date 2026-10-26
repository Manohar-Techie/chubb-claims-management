package com.claim.chubb.repository;

import com.claim.chubb.entity.Claim;
import com.claim.chubb.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}