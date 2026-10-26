package com.claim.chubb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_information_requests")
public class ClaimInformationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(length = 4000)
    private String response;

    private LocalDateTime responseAt;

    protected ClaimInformationRequest() {
    }

    public ClaimInformationRequest(Claim claim, String description) {
        this.claim = claim;
        this.description = description;
        this.requestedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public String getResponse() {
        return response;
    }

    public LocalDateTime getResponseAt() {
        return responseAt;
    }

    public void provideResponse(String response) {
        this.response = response;
        this.responseAt = LocalDateTime.now();
    }
}
