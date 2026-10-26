package com.claim.chubb.entity;

import com.claim.chubb.enums.DecisionValue;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "claim_decisions")
public class ClaimDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DecisionValue decision;

    @Column(nullable = false, length = 100)
    private String decisionMakerIdentifier;

    @Column(nullable = false, length = 2000)
    private String decisionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime decidedAt;

    protected ClaimDecision() {
    }

    public ClaimDecision(Claim claim, DecisionValue decision,
                         String decisionMakerIdentifier, String decisionReason) {
        this.claim = claim;
        this.decision = decision;
        this.decisionMakerIdentifier = decisionMakerIdentifier;
        this.decisionReason = decisionReason;
        this.decidedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Claim getClaim() {
        return claim;
    }

    public DecisionValue getDecision() {
        return decision;
    }

    public String getDecisionMakerIdentifier() {
        return decisionMakerIdentifier;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }
}
