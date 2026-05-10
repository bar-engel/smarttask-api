# SmartTask API

A production-ready task management REST API built with Java 17 and Spring Boot 3. Supports JWT authentication, role-based access control, full task CRUD with filtering, and interactive Swagger documentation — all deployable with a single Docker command.

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![License](https://img.shields.io/badge/license-MIT-green)

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [Request & Response Examples](#request--response-examples)
- [Architecture](#architecture)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)
- [Security Notes](#security-notes)

---

## Features

- JWT-based stateless authentication with 24-hour token expiration
- Role-based access control (USER / ADMIN)
- Full CRUD for tasks with status and priority filtering
- Task statistics endpoint (counts by status)
- Global exception handling with field-level validation errors
- Swagger UI with interactive Bearer token authentication
- Multi-stage Docker build + Docker Compose for one-command deployment
- Unit tests (services) and integration tests (controllers) using H2 in-memory database

---

## Tech Stack

| Technology          | Version | Purpose                        |
|---------------------|---------|--------------------------------|
| Java                | 17      | Core language                  |
| Spring Boot         | 3.2.5   | Web framework                  |
| Spring Security     | 6.x     | Authentication & authorization |
| PostgreSQL          | 15      | Primary database               |
| JWT (jjwt)          | 0.12.3  | Token generation & validation  |
| Docker / Compose    | latest  | Containerization               |
| Swagger / OpenAPI   | 2.3.0   | Interactive API documentation  |
| JUnit 5             | latest  | Unit & integration testing     |
| H2                  | latest  | In-memory database for tests   |
| Lombok              | latest  | Boilerplate reduction          |

---

## Getting Started

### Prerequisites

- Docker & Docker Compose

That is the only hard requirement for the recommended setup. Java 17 and Maven 3.8+ are only needed for local development.

---

### Option 1 — Docker (Recommended)

```bash
docker-compose up --build
```

This spins up PostgreSQL and the API together. The application will be available at `http://localhost:8080`.

---

### Option 2 — Local Development

**1. Start the database**

```bash
docker-compose up postgres
```

Or point a local PostgreSQL instance at:

| Setting  | Value            |
|----------|------------------|
| Database | `smarttask_db`   |
| Username | `smarttask_user` |
| Password | `smarttask_pass` |
| Port     | `5432`           |

**2. Run the application**

```bash
mvn spring-boot:run
```

---

## Environment Variables

Override any of these at runtime (Docker Compose, Kubernetes, or shell export):

| Variable                     | Default                                        | Description               |
|------------------------------|------------------------------------------------|---------------------------|
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:5432/smarttask_db`| Database connection URL   |
| `SPRING_DATASOURCE_USERNAME` | `smarttask_user`                               | Database username         |
| `SPRING_DATASOURCE_PASSWORD` | `smarttask_pass`                               | Database password         |
| `APP_JWT_SECRET`             | *(see application.properties)*                 | HMAC-SHA256 signing key   |
| `APP_JWT_EXPIRATION`         | `86400000`                                     | Token TTL in milliseconds |

> **Production note:** Always replace `APP_JWT_SECRET` with a securely generated value before deploying.

---

## API Endpoints

### Authentication — no token required

| Method | Endpoint               | Description                       | Response      |
|--------|------------------------|-----------------------------------|---------------|
| POST   | `/api/auth/register`   | Register a new user               | `201` + JWT   |
| POST   | `/api/auth/login`      | Login and receive a JWT token     | `200` + JWT   |

### Tasks — JWT required

| Method | Endpoint               | Description                                               | Response      |
|--------|------------------------|-----------------------------------------------------------|---------------|
| GET    | `/api/tasks`           | List your tasks (filter by `?status=` or `?priority=`)    | `200`         |
| POST   | `/api/tasks`           | Create a new task                                         | `201`         |
| GET    | `/api/tasks/stats`     | Task counts grouped by status                             | `200`         |
| GET    | `/api/tasks/{id}`      | Get a specific task by ID                                 | `200`         |
| PUT    | `/api/tasks/{id}`      | Update a task (only non-null fields applied)              | `200`         |
| DELETE | `/api/tasks/{id}`      | Delete a task                                             | `204`         |

### Users — JWT required

| Method | Endpoint               | Auth Required | Description                    |
|--------|------------------------|---------------|--------------------------------|
| GET    | `/api/users/me`        | Any user      | Get the current user's profile |
| GET    | `/api/users`           | ADMIN only    | List all registered users      |

### Task Filter Parameters

`GET /api/tasks` supports optional query parameters:

| Parameter  | Accepted Values               | Example                          |
|------------|-------------------------------|----------------------------------|
| `status`   | `TODO`, `IN_PROGRESS`, `DONE` | `GET /api/tasks?status=TODO`     |
| `priority` | `LOW`, `MEDIUM`, `HIGH`       | `GET /api/tasks?priority=HIGH`   |

---

## Request & Response Examples

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "secret123"
}
```

```json
HTTP/1.1 201 Created

{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john",
  "role": "USER"
}
```

### Create a Task

```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Write unit tests",
  "description": "Cover service and controller layers",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-06-01"
}
```

```json
HTTP/1.1 201 Created

{
  "id": 1,
  "title": "Write unit tests",
  "description": "Cover service and controller layers",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-06-01",
  "createdAt": "2025-05-10T12:00:00",
  "updatedAt": "2025-05-10T12:00:00"
}
```

### Validation Error

```json
HTTP/1.1 400 Bad Request

{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-05-10T12:00:00",
  "errors": {
    "title": "must not be blank",
    "password": "size must be between 8 and 2147483647"
  }
}
```

---

## Architecture

The application follows a classic **layered architecture**:

```
HTTP Request
      │
      ▼
┌─────────────┐
│  Controller │  Validates input (@Valid), delegates to service, returns ResponseEntity
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  Business logic, ownership checks, maps entities ↔ DTOs via TaskMapper
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │  Spring Data JPA — derived queries, no boilerplate SQL
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Database   │  PostgreSQL (H2 for tests)
└─────────────┘
```

**Security filter chain** (executes before the controller on every request):

```
Incoming Request
      │
      ▼
JwtAuthFilter
      │  Extracts Bearer token from Authorization header
      │  Validates signature + expiration via JwtService
      │  Loads UserDetails from database
      │  Populates SecurityContext
      ▼
SecurityFilterChain
      │  Permits: /api/auth/**, /swagger-ui/**, /api-docs/**
      │  Requires authentication: everything else
      ▼
Controller
```

**Exception handling flow:**

```
Service throws RuntimeException
        │
        ▼
GlobalExceptionHandler (@RestControllerAdvice)
        │
        └─ Maps exception type → HTTP status + ErrorResponse JSON
```

### Project Structure

```
src/main/java/com/smarttask/
├── controller/          # REST controllers — HTTP handling only
├── service/             # Business logic + TaskMapper (entity ↔ DTO)
├── repository/          # Spring Data JPA interfaces
├── model/
│   ├── entity/          # User, Task entities; Role, TaskStatus, TaskPriority enums
│   └── dto/
│       ├── request/     # RegisterRequest, LoginRequest, CreateTaskRequest, UpdateTaskRequest
│       └── response/    # AuthResponse, TaskResponse, UserResponse
├── security/
│   ├── jwt/             # JwtService, JwtAuthFilter
│   └── UserDetailsServiceImpl.java
├── config/              # SecurityConfig, SwaggerConfig, DataInitializer
└── exception/           # GlobalExceptionHandler, custom exceptions, ErrorResponse
```

---

## Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests (no Spring context required)
mvn test -Dtest="TaskServiceTest,AuthServiceTest"

# Run only integration tests
mvn test -Dtest="TaskControllerTest"
```

Tests use an **H2 in-memory database** (activated by the `test` Spring profile), so no running PostgreSQL is needed.

| Test Class           | Type        | Coverage                                  |
|----------------------|-------------|-------------------------------------------|
| `AuthServiceTest`    | Unit        | register, login, duplicate user detection |
| `TaskServiceTest`    | Unit        | createTask, getById, update, delete       |
| `TaskControllerTest` | Integration | All endpoints — status codes, JSON shape  |

---

## API Documentation

After startup, interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI spec (JSON):

```
http://localhost:8080/api-docs
```

Click **Authorize** in the Swagger UI and paste your JWT token (without the `Bearer ` prefix) to authenticate all subsequent requests directly from the browser.

---

## Security Notes

- Passwords are hashed with **BCrypt** — plain text is never stored.
- JWT tokens are signed with **HMAC-SHA256** using the secret in `app.jwt.secret`.
- Tokens expire after **24 hours** (`app.jwt.expiration=86400000` ms).
- CSRF protection is disabled — correct for a stateless JWT API consumed by SPAs or mobile clients.
- Each task endpoint verifies ownership — users cannot read, modify, or delete tasks belonging to another user.
- Replace the default JWT secret with a cryptographically secure value before any production deployment.
