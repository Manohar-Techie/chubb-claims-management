package com.claim.chubb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_assignments")
public class ClaimAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(nullable = false, length = 100)
    private String officerIdentifier;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    protected ClaimAssignment() {
    }

    public ClaimAssignment(Claim claim, String officerIdentifier) {
        this.claim = claim;
        this.officerIdentifier = officerIdentifier;
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getOfficerIdentifier() {
        return officerIdentifier;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}