package com.claim.chubb.entity;

import com.claim.chubb.enums.ClaimStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String claimReference;

    @Column(nullable = false)
    private String claimantIdentifier;

    @Column(nullable = false)
    private String claimType;

    @Column(nullable = false)
    private String policyReference;

    private LocalDate incidentDate;

    private String incidentLocation;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedLoss;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(precision = 15, scale = 2)
    private BigDecimal assessedLiability;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    private String assignedOfficerIdentifier;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public  Claim() {
    }

    @PrePersist
    public void onCreate() {
        if (claimReference == null) {
            claimReference = "CLM-" +
                    UUID.randomUUID().toString()
                            .substring(0, 8).toUpperCase();
        }

        createdAt = LocalDateTime.now();
        updatedAt = createdAt;

        if (status == null) {
            status = ClaimStatus.SUBMITTED;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getClaimReference() {
        return claimReference;
    }

    public String getClaimantIdentifier() {
        return claimantIdentifier;
    }

    public String getClaimType() {
        return claimType;
    }

    public String getPolicyReference() {
        return policyReference;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public String getIncidentLocation() {
        return incidentLocation;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getEstimatedLoss() {
        return estimatedLoss;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAssessedLiability() {
        return assessedLiability;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public String getAssignedOfficerIdentifier() {
        return assignedOfficerIdentifier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setClaimantIdentifier(String value) {
        this.claimantIdentifier = value;
    }

    public void setClaimType(String value) {
        this.claimType = value;
    }

    public void setPolicyReference(String value) {
        this.policyReference = value;
    }

    public void setIncidentDate(LocalDate value) {
        this.incidentDate = value;
    }

    public void setIncidentLocation(String value) {
        this.incidentLocation = value;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public void setEstimatedLoss(BigDecimal value) {
        this.estimatedLoss = value;
    }

    public void setCurrency(String value) {
        this.currency = value;
    }

    public void setAssignedOfficerIdentifier(String officerIdentifier) {
        this.assignedOfficerIdentifier = officerIdentifier;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public void setAssessedLiability(BigDecimal assessedLiability) {
        this.assessedLiability = assessedLiability;
    }
}