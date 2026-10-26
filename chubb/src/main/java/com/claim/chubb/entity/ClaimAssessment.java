package com.claim.chubb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_assessments")
public class ClaimAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal assessedLiability;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String rationale;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String assessorOfficerIdentifier;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assessedAt;

    protected ClaimAssessment() {
    }

    public ClaimAssessment(Claim claim, BigDecimal assessedLiability, String currency,
                          String rationale, String assessorOfficerIdentifier) {
        this.claim = claim;
        this.assessedLiability = assessedLiability;
        this.currency = currency;
        this.rationale = rationale;
        this.assessorOfficerIdentifier = assessorOfficerIdentifier;
        this.assessedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Claim getClaim() {
        return claim;
    }

    public BigDecimal getAssessedLiability() {
        return assessedLiability;
    }

    public String getCurrency() {
        return currency;
    }

    public String getRationale() {
        return rationale;
    }

    public String getAssessorOfficerIdentifier() {
        return assessorOfficerIdentifier;
    }

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }
}
