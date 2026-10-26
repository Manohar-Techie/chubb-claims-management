package com.claim.chubb.entity;

import com.claim.chubb.enums.ClaimStatus;
import com.claim.chubb.enums.ClaimType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Check(constraints = "estimated_loss >= 0 AND (assessed_liability IS NULL OR assessed_liability >= 0)")
@Table(
        name = "claims",
        indexes = {
                @Index(name = "idx_claims_claimant", columnList = "claimant_identifier"),
                @Index(name = "idx_claims_status_created", columnList = "claim_status, created_at"),
                @Index(name = "idx_claims_officer_status", columnList = "assigned_officer_identifier, claim_status"),
                @Index(name = "idx_claims_market_status", columnList = "market_code, claim_status")
        }
)
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "claim_reference", nullable = false, length = 36, unique = true)
    @Size(max = 36)
    private String claimReference;

    @NotBlank
    @Column(name = "claimant_identifier", nullable = false, length = 64)
    @Size(max = 64)
    private String claimantIdentifier;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false, length = 16)
    private ClaimType claimType;

    @NotBlank
    @Column(name = "market_code", nullable = false, length = 16)
    @Size(max = 16)
    private String marketCode;

    @NotBlank
    @Column(name = "policy_reference", nullable = false, length = 64)
    @Size(max = 64)
    private String policyReference;

    @NotNull
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @NotBlank
    @Column(name = "incident_location", nullable = false, length = 255)
    @Size(max = 255)
    private String incidentLocation;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "text")
    @Size(max = 4000)
    private String description;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 17, fraction = 2)
    @Column(name = "estimated_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal estimatedLoss;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @DecimalMin("0.00")
    @Digits(integer = 17, fraction = 2)
    @Column(name = "assessed_liability", precision = 19, scale = 2)
    private BigDecimal assessedLiability;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", nullable = false, length = 32)
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Size(max = 64)
    @Column(name = "assigned_officer_identifier", length = 64)
    private String assignedOfficerIdentifier;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getClaimReference() {
        return claimReference;
    }

    public void setClaimReference(String claimReference) {
        this.claimReference = claimReference;
    }

    public String getClaimantIdentifier() {
        return claimantIdentifier;
    }

    public void setClaimantIdentifier(String claimantIdentifier) {
        this.claimantIdentifier = claimantIdentifier;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public String getMarketCode() {
        return marketCode;
    }

    public void setMarketCode(String marketCode) {
        this.marketCode = marketCode;
    }

    public String getPolicyReference() {
        return policyReference;
    }

    public void setPolicyReference(String policyReference) {
        this.policyReference = policyReference;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getIncidentLocation() {
        return incidentLocation;
    }

    public void setIncidentLocation(String incidentLocation) {
        this.incidentLocation = incidentLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getEstimatedLoss() {
        return estimatedLoss;
    }

    public void setEstimatedLoss(BigDecimal estimatedLoss) {
        this.estimatedLoss = estimatedLoss;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAssessedLiability() {
        return assessedLiability;
    }

    public void setAssessedLiability(BigDecimal assessedLiability) {
        this.assessedLiability = assessedLiability;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getAssignedOfficerIdentifier() {
        return assignedOfficerIdentifier;
    }

    public void setAssignedOfficerIdentifier(String assignedOfficerIdentifier) {
        this.assignedOfficerIdentifier = assignedOfficerIdentifier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
