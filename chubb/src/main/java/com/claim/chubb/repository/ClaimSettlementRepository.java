package com.claim.chubb.repository;

import com.claim.chubb.entity.ClaimSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimSettlementRepository extends JpaRepository<ClaimSettlement, Long> {

    List<ClaimSettlement> findByClaimClaimReferenceOrderBySettledAtAscIdAsc(
            String claimReference);

    boolean existsByPaymentReference(String paymentReference);

    boolean existsByClaimClaimReference(String claimReference);
}
