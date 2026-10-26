package com.claim.chubb.repository;

import com.claim.chubb.entity.ClaimDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimDecisionRepository extends JpaRepository<ClaimDecision, Long> {

    List<ClaimDecision> findByClaimClaimReferenceOrderByDecidedAtAscIdAsc(
            String claimReference);
}
