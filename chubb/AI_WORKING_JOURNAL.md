# AI Working Journal

## Project

Chubb APAC Claims Management Backend

## Technology

* Java 21
* Spring Boot
* Maven
* MySQL
* Spring Data JPA
* Git and GitHub
* IntelliJ IDEA
* GitHub Copilot

## Session 1: Project Initialization

### Objective

Create the initial Spring Boot project using Spring Initializr.

### AI assistance

Used ChatGPT to plan the project structure, implementation sequence, and Git workflow.

### Decision

Selected a layered modular monolith.

### Reasoning

The assessment has a 2–3 hour target. A modular monolith allows the core claims workflow to be implemented without the operational complexity of multiple independently deployed services.

### Verification

Pending.

## Session 2: Database and Domain Model

### Objective

Design the claims database schema, entities, repositories, and status lifecycle.

### AI assistance

Used GitHub Copilot to implement the initial claims persistence model after reviewing the existing Maven project configuration.

### Decisions accepted or challenged

Retained existing Spring Data JPA, validation, and MySQL dependencies; no dependency changes were needed. Per the project direction, Flyway was excluded from this milestone. Database credentials use environment-variable placeholders. Market codes remain unrestricted strings because the assessment does not enumerate them.

### Reasoning

JPA schema update is enabled for this development milestone in place of Flyway. Monetary fields use BigDecimal-compatible DECIMAL precision, status and type are persisted as strings, common operational lookup paths are indexed, and a version field enables optimistic locking. No authentication or officer directory was added.

### Verification

Maven compile completed successfully with `.\chubb\mvnw.cmd -f .\chubb\pom.xml -DskipTests compile`. Database connection has not been tested.

## Session 3: Claims Workflow

### Objective

Implement claim submission, assignment, information requests, assessments, decisions, and settlement.

### AI assistance

Pending.

### Decisions accepted or challenged

Pending.

### Reasoning

Pending.

### Verification

Pending.

## Session 4: Dashboard and Testing

### Objective

Implement management reporting, liability exposure, validation, exception handling, and automated tests.

### AI assistance

Pending.

### Decisions accepted or challenged

Pending.

### Reasoning

Pending.

### Verification

Pending.
