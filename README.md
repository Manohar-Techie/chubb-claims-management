# Chubb Claims API

A Spring Boot REST API for managing insurance claims and their workflow, including assignment, information requests, assessments, decisions, settlements, audit history, and dashboard summaries.

## Requirements

- Java 21
- MySQL

## Configure the database

Create a MySQL database named `chubb`. The application connects to `jdbc:mysql://localhost:3306/chubb` by default. Set the database username and password through environment variables before starting the application:

```powershell
$env:SPRING_DATASOURCE_USERNAME = "your-mysql-user"
$env:SPRING_DATASOURCE_PASSWORD = "your-mysql-password"
```

Spring Boot uses these variables to override the datasource settings in `application.properties`.

## Run the application

From the repository root, run:

```powershell
cd chubb
.\mvnw.cmd spring-boot:run
```

The API listens on port `8002` by default. The Maven wrapper is also available as `./mvnw` on macOS and Linux.

## API

All claim endpoints use the base path `/api/v1/claims`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/claims` | Submit a claim |
| `GET` | `/api/v1/claims` | List claims; optionally filter with `?status=...` |
| `GET` | `/api/v1/claims/{claimReference}` | Get a claim |
| `PUT` | `/api/v1/claims/{claimReference}/assignment` | Assign a claim |
| `GET` | `/api/v1/claims/{claimReference}/assignments` | Get assignment history |
| `POST`, `GET` | `/api/v1/claims/{claimReference}/information-requests` | Request or list information requests |
| `POST` | `/api/v1/claims/{claimReference}/information-requests/{requestId}/response` | Respond to an information request |
| `POST`, `GET` | `/api/v1/claims/{claimReference}/assessments` | Add an assessment or list assessment history |
| `POST` | `/api/v1/claims/{claimReference}/decision` | Record a claim decision |
| `GET` | `/api/v1/claims/{claimReference}/decisions` | Get decision history |
| `POST`, `GET` | `/api/v1/claims/{claimReference}/settlements` | Create a settlement or list settlements |
| `GET` | `/api/v1/claims/{claimReference}/audit-logs` | Get claim audit history |
| `GET` | `/api/v1/claims/dashboard` | Get dashboard summary |

Requests and responses use JSON. Request DTOs in `src/main/java/com/claim/chubb/dto` define the accepted fields and validation rules.

## Project structure

- `src/main/java/com/claim/chubb/controller` — REST endpoints
- `src/main/java/com/claim/chubb/service` — claim workflow and business logic
- `src/main/java/com/claim/chubb/entity` and `repository` — persistence model and data access
- `src/main/resources` — application configuration and database schema

