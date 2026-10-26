package com.claim.chubb.entity;

import com.claim.chubb.enums.ClaimStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String claimReference;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String action;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String performedByIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ClaimStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ClaimStatus newStatus;

    @Size(max = 4000)
    @Column(length = 4000)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    protected AuditLog() {
    }

    public AuditLog(String claimReference, String action, String performedByIdentifier,
                    ClaimStatus previousStatus, ClaimStatus newStatus, String details) {
        this.claimReference = claimReference;
        this.action = action;
        this.performedByIdentifier = performedByIdentifier;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.details = details;
        this.occurredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getClaimReference() {
        return claimReference;
    }

    public String getAction() {
        return action;
    }

    public String getPerformedByIdentifier() {
        return performedByIdentifier;
    }

    public ClaimStatus getPreviousStatus() {
        return previousStatus;
    }

    public ClaimStatus getNewStatus() {
        return newStatus;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
