Chubb Claims Management API

A RESTful backend application for managing motor and property insurance claims. The application supports the claim lifecycle, from submission and assignment through assessment, decision-making, settlement, and audit history.

## Project Overview

The Chubb Claims Management API provides endpoints for managing insurance claims and enforcing business workflow rules.

Key Features
Submit and retrieve insurance claims.
Assign claims to officers and retrieve assignment history.
Request additional information and record responses.
Assess claim liability and maintain assessment history.
Approve or reject assessed claims.
Record settlements for approved claims.
Maintain audit history of important claim operations.
Provide dashboard summaries of claims, officer workloads, and outstanding liability.
Validate API requests and handle exceptions consistently.
Use API versioning through the /api/v1 base path.
## Technology Stack
   Technology	Purpose
   Java 21	Programming language
   Spring Boot	Application framework
   Spring Web	REST API development
   Spring Data JPA	Database access
   Hibernate ORM	Object-relational mapping
   MySQL 8	Relational database
   Jakarta Bean Validation	Request validation
   Maven	Dependency management and build
   Postman	API testing
   IntelliJ IDEA	Development environment
## Architecture

The application follows a layered monolithic architecture.

Client (Postman / Frontend)
|
v
REST Controller
|
v
Service Layer
|
v
Repository Layer
|
v
MySQL Database
## Layer Responsibilities

## Controller Layer

Exposes REST endpoints.
Accepts and validates HTTP requests.
Returns appropriate HTTP responses.

## Service Layer

Implements business rules.
Validates claim workflow transitions.
Coordinates database operations.
Uses transactions for operations that must succeed or fail together.

## Repository Layer

Uses Spring Data JPA to access the database.
Retrieves and persists entities.

## Entity Layer

Represents database records, including claims, assessments, decisions, settlements, and audit logs.

## DTO Layer

Defines request and response structures.
Keeps API contracts separate from database entities.

## Exception Handling

Provides centralized handling for validation failures, missing resources, invalid workflow operations, and unexpected errors.
## Prerequisites

Install the following before running the application:

JDK 21
MySQL 8
IntelliJ IDEA or another Java IDE
Git, if cloning the repository

Verify Java and Maven Wrapper availability:

java -version

The project includes a Maven Wrapper, so a separate Maven installation is not required.

## Database Setup

Create the database in MySQL:

CREATE DATABASE chubb;

The application expects MySQL to be available locally on port 3306, unless the datasource configuration is changed.

Configure Database Credentials

Open:

src/main/resources/application.properties

Configure the datasource:

spring.application.name=chubb
server.port=8002

spring.datasource.url=jdbc:mysql://localhost:3306/chubb
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

Set the following environment variables in your IDE or terminal:

DB_USERNAME: Your MySQL username.
DB_PASSWORD: Your MySQL password.

Do not commit real database passwords to source control.

Database schema: Hibernate's update setting manages compatible schema changes automatically. Review database migrations and schema changes carefully in production. This project does not require Flyway for the current assessment implementation.

## Running the Application

   Clone the Repository

If you are using Git:

git clone <your-repository-url>

Open the project directory containing pom.xml.

Run Tests

On Windows:

.\mvnw.cmd clean test

On Linux or macOS:

./mvnw clean test
Start the Application

Run ChubbApplication.java from IntelliJ IDEA.

Alternatively, use the Maven Wrapper.

Windows:

.\mvnw.cmd spring-boot:run

Linux or macOS:

./mvnw spring-boot:run

The application is configured to run on:

http://localhost:8002

Confirm successful startup in the console before testing endpoints.

7. API Reference

All endpoints use the base path:

http://localhost:8002/api/v1/claims

## Claim Management
Method	Endpoint	Description
POST	/api/v1/claims	Submit a claim
GET	/api/v1/claims	Retrieve all claims
GET	/api/v1/claims?status=UNDER_REVIEW	Filter claims by status
GET	/api/v1/claims/{claimReference}	Retrieve a claim by reference
7.2 Claim Assignment
Method	Endpoint	Description
PUT	/api/v1/claims/{claimReference}/assignment	Assign a claim to an officer
GET	/api/v1/claims/{claimReference}/assignments	Retrieve assignment history
7.3 Additional Information
Method	Endpoint	Description
POST	/api/v1/claims/{claimReference}/information-requests	Request additional information
GET	/api/v1/claims/{claimReference}/information-requests	Retrieve information requests
POST	/api/v1/claims/{claimReference}/information-requests/{requestId}/response	Record a response to an information request
7.4 Claim Assessment
Method	Endpoint	Description
POST	/api/v1/claims/{claimReference}/assessments	Create a claim assessment
GET	/api/v1/claims/{claimReference}/assessments	Retrieve assessment history
7.5 Claim Decision
Method	Endpoint	Description
POST	/api/v1/claims/{claimReference}/decision	Approve or reject a claim
GET	/api/v1/claims/{claimReference}/decisions	Retrieve decision history
7.6 Settlement
Method	Endpoint	Description
POST	/api/v1/claims/{claimReference}/settlements	Record a claim settlement
GET	/api/v1/claims/{claimReference}/settlements	Retrieve settlement details
7.7 Audit History
Method	Endpoint	Description
GET	/api/v1/claims/{claimReference}/audit-logs	Retrieve audit history for a claim
7.8 Dashboard
Method	Endpoint	Description
GET	/api/v1/claims/dashboard	Retrieve claim dashboard summary

The dashboard is intended to report total claims, counts by status, officer workload, and outstanding assessed liability grouped by currency.

Note: Confirm the dashboard endpoint and each listed route against the final source code before submitting the assessment.

## Claim Lifecycle

The main claim lifecycle is:

SUBMITTED
|
v
UNDER_REVIEW
|
+------------------------+
|                        |
v                        |
INFORMATION_REQUIRED         |
|                        |
v                        |
UNDER_REVIEW <---------------+
|
v
ASSESSED
|
+-------------+
|             |
v             v
APPROVED      REJECTED
|
v
SETTLED

Additional information is requested only when necessary.

Workflow Rules
Claims must exist before they can be assigned, assessed, or decided.
Assessment is permitted only when a claim is under review.
A claim must be assessed before a decision can be recorded.
A decision must approve or reject the claim.
Only approved claims can be settled.
A settled claim must not be settled again under the single-settlement workflow.
Assessment and audit history should preserve previous records.
Financial amounts must be grouped by currency rather than combined across currencies.

The application should enforce these rules in the service layer.

## Request Examples

The examples below illustrate the intended API contracts. Confirm field names against the actual request DTOs in the project.

Create an Assessment
POST /api/v1/claims/{claimReference}/assessments
Content-Type: application/json
{
"assessedLiability": 45000.00,
"currency": "INR",
"rationale": "Repair estimate reviewed and liability determined.",
"assessorOfficerIdentifier": "OFFICER002"
}
Approve a Claim
POST /api/v1/claims/{claimReference}/decision
Content-Type: application/json
{
"decision": "APPROVE",
"decisionMakerIdentifier": "OFFICER001",
"decisionReason": "Assessment reviewed and claim approved."
}
Record a Settlement
POST /api/v1/claims/{claimReference}/settlements
Content-Type: application/json
{
"settlementAmount": 45000.00,
"currency": "INR",
"paymentReference": "PAY-2026-001",
"processedBy": "OFFICER001"
}

Use an existing claim reference and values consistent with the claim's current status and assessed liability.


Actual responses depend on the endpoint and the application's exception-handler implementation.

## Testing with Postman
    Start MySQL.
    Start the Spring Boot application.
    Create or retrieve a claim.
    Assign the claim to an officer.
    Request and provide additional information if needed.
    Assess the claim.
    Approve or reject the claim.
    If approved, record its settlement.
    Retrieve the audit history.
    Retrieve the dashboard summary.

Use the claim reference returned by the API rather than assuming a reference value.


## Future Improvements

Potential future enhancements include:

Authentication and role-based authorization.
Pagination and sorting for claim retrieval.
Optimistic locking and concurrency tests.
Database migrations with Flyway.
Integration tests using a dedicated test database or Testcontainers.
OpenAPI/Swagger documentation.
Idempotency for payment and settlement operations.
Kafka-based asynchronous event processing.
Docker-based local development.
CI/CD automation.
Monitoring, metrics, and structured logging.
14. Kafka Architecture

Kafka is not required for the current synchronous implementation.
