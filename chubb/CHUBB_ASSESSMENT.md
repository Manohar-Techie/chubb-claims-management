# Chubb APAC Claims Management Backend

## 1. Objective

Build a locally runnable backend for managing motor and property insurance claims.

Claimants need to submit claims, track their status, provide additional information, and receive decisions.

Claims officers need to review claims, manage assignments, assess liability, and approve, reject, or settle claims.

Managers need visibility into claim workload and outstanding liability exposure.

## 2. Technology

* Java 21
* Spring Boot
* Maven
* MySQL
* Spring Data JPA
* Jakarta Bean Validation
* JUnit 5 and Mockito

## 3. Architecture

Use a simple layered monolith.

Root package: `com.claim.chubb`

Packages:

* controller
* service
* repository
* entity
* dto
* enums
* exception

Use configuration classes only when necessary.

## 4. Functional Requirements

### Claim Management

* Submit motor and property claims.
* Generate a unique claim reference.
* Retrieve a claim by reference.
* List claims 
* Track claim status.

### Additional Information

* Request additional information.
* Record the claimant's response.
* Return the claim to review after the response.

### Assignment

* Assign a claim to a claims officer.
* Track the assigned officer.
* Retrieve claims assigned to an officer.

### Assessment and Decisions

* Record assessed liability.
* Record the assessment rationale.
* Approve or reject claims.
* Require a valid assessment before the final decision.
* Permit settlement only after approval.
* Reject invalid lifecycle transitions.

### Manager Dashboard

* Count claims by status.
* Summarize workload.
* Calculate outstanding liability exposure.

## 5. Claim Lifecycle

Proposed statuses:

SUBMITTED
UNDER_REVIEW
INFORMATION_REQUIRED
ASSESSED
DECISION_PENDING
APPROVED
REJECTED
SETTLED

A new claim starts as SUBMITTED.

Assignment to an officer moves a submitted claim to UNDER_REVIEW.

An information request moves a claim to INFORMATION_REQUIRED.

After the claimant provides the requested information, the claim returns to UNDER_REVIEW.

Assessment records the liability and moves the claim to ASSESSED.

A decision moves the claim to DECISION_PENDING before the claim becomes APPROVED or REJECTED.

Only APPROVED claims can become SETTLED.

These transitions are implementation assumptions because the assessment does not specify every lifecycle rule.

## 6. Financial Rules

* Use BigDecimal for monetary values.
* Keep estimated loss separate from assessed liability.
* Store currency explicitly.
* Do not combine different currencies into one exposure total.

For the initial implementation, outstanding liability exposure is defined as the assessed liability of approved claims that have not been settled, grouped by currency.

## 7. Engineering Requirements

* REST endpoints under /api.
* Use DTOs for requests and responses.
* Keep controllers thin.
* Put business rules in services.
* Use Spring Data JPA repositories.
* Validate incoming requests.
* Implement global exception handling.
* Use transactions for business operations.
* Use optimistic locking where appropriate.
* Record important lifecycle changes in audit history.
* Add tests for critical business rules.

## 8. Explicit Exclusions

* No frontend.
* No AppUser entity.
* No authentication implementation.
* No Spring Security configuration.
* No unnecessary microservices.
* No Kafka broker implementation.
* No Redis.
* No payment gateway.

Kafka may be documented as a future option for notifications, analytics, and integrations.

## 9. Important Limitation

Authentication is outside this implementation scope.

Claimant and officer identifiers are references only. Accepting an identifier does not prove the caller's identity or ownership of a claim.

Document this limitation rather than claiming that the API provides secure ownership verification.

## 10. Development Constraints

* Keep the solution simple and understandable.
* Implement one feature at a time.
* Avoid unnecessary dependencies.
* Do not expose JPA entities directly through controllers.
* Do not invent market codes or unsupported business requirements.
* Do not claim that code or tests work without running them.
* Keep the application runnable throughout development.
* Update AI_WORKING_JOURNAL.md as work progresses.
* Use meaningful Git commits after reviewing and testing each milestone.
