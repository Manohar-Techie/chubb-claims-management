package com.claim.chubb.repository;

import com.claim.chubb.entity.ClaimAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimAssessmentRepository extends JpaRepository<ClaimAssessment, Long> {

    List<ClaimAssessment> findByClaimClaimReferenceOrderByAssessedAtAscIdAsc(
            String claimReference);
}
