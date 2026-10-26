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
@Table(name = "claim_settlements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_claim_settlement_claim", columnNames = "claim_id")
})
public class ClaimSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 13, fraction = 2)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal settlementAmount;

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String paymentReference;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String processedByOfficerIdentifier;

    @Column(nullable = false, updatable = false)
    private LocalDateTime settledAt;

    protected ClaimSettlement() {
    }

    public ClaimSettlement(Claim claim, BigDecimal settlementAmount, String currency,
                           String paymentReference, String processedByOfficerIdentifier) {
        this.claim = claim;
        this.settlementAmount = settlementAmount;
        this.currency = currency;
        this.paymentReference = paymentReference;
        this.processedByOfficerIdentifier = processedByOfficerIdentifier;
        this.settledAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Claim getClaim() {
        return claim;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public String getProcessedByOfficerIdentifier() {
        return processedByOfficerIdentifier;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }
}
