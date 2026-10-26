package com.claim.chubb.repository;

import com.claim.chubb.entity.ClaimInformationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimInformationRequestRepository
        extends JpaRepository<ClaimInformationRequest, Long> {

    List<ClaimInformationRequest> findByClaimClaimReferenceOrderByRequestedAtAsc(
            String claimReference);

    long countByClaimClaimReferenceAndResponseIsNull(String claimReference);
}
