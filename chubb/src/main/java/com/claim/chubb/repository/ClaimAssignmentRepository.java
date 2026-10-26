package com.claim.chubb.repository;

import com.claim.chubb.entity.ClaimAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimAssignmentRepository
        extends JpaRepository<ClaimAssignment, Long> {

    List<ClaimAssignment> findByClaimClaimReferenceOrderByAssignedAtDesc(
            String claimReference);
}