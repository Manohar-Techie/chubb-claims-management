CREATE TABLE claims (
    id BIGINT NOT NULL AUTO_INCREMENT,
    claim_reference VARCHAR(36) NOT NULL,
    claimant_identifier VARCHAR(64) NOT NULL,
    claim_type VARCHAR(16) NOT NULL,
    market_code VARCHAR(16) NOT NULL,
    policy_reference VARCHAR(64) NOT NULL,
    incident_date DATE NOT NULL,
    incident_location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    estimated_loss DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    assessed_liability DECIMAL(19, 2) NULL,
    claim_status VARCHAR(32) NOT NULL,
    assigned_officer_identifier VARCHAR(64) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT pk_claims PRIMARY KEY (id),
    CONSTRAINT uk_claims_claim_reference UNIQUE (claim_reference),
    CONSTRAINT chk_claims_amounts CHECK (
        estimated_loss >= 0
        AND (assessed_liability IS NULL OR assessed_liability >= 0)
    ),
    INDEX idx_claims_claimant (claimant_identifier),
    INDEX idx_claims_status_created (claim_status, created_at),
    INDEX idx_claims_officer_status (assigned_officer_identifier, claim_status),
    INDEX idx_claims_market_status (market_code, claim_status)
);
