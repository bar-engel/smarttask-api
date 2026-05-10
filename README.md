# SmartTask API

A production-ready REST API for task management, built with Java 17, Spring Boot 3, and PostgreSQL.

## Features

- JWT Authentication & Authorization
- Full CRUD for task management
- Role-based access control (USER / ADMIN)
- Input validation with detailed error responses
- Global exception handling
- Dockerized deployment
- Swagger UI documentation
- Unit & Integration tests

## Tech Stack

| Technology       | Version | Purpose               |
|------------------|---------|-----------------------|
| Java             | 17      | Core language         |
| Spring Boot      | 3.2.5   | Framework             |
| Spring Security  | 6.x     | Authentication        |
| PostgreSQL       | 15      | Database              |
| JWT (jjwt)       | 0.12.3  | Token management      |
| Docker           | latest  | Containerization      |
| Swagger/OpenAPI  | 2.3.0   | API Documentation     |
| JUnit 5          | latest  | Testing               |
| Lombok           | latest  | Boilerplate reduction |

## Project Structure

```
src/main/java/com/smarttask/
├── controller/          # REST controllers — HTTP request/response handling
├── service/             # Business logic + TaskMapper DTO converter
├── repository/          # Spring Data JPA interfaces
├── model/
│   ├── entity/          # JPA entities (User, Task) and enums
│   └── dto/
│       ├── request/     # RegisterRequest, LoginRequest, CreateTaskRequest, UpdateTaskRequest
│       └── response/    # AuthResponse, TaskResponse, UserResponse
├── security/
│   ├── jwt/             # JwtService, JwtAuthFilter
│   └── UserDetailsServiceImpl.java
├── config/              # SecurityConfig, OpenApiConfig
└── exception/           # GlobalExceptionHandler, custom exceptions, ErrorResponse
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for containerized setup)

---

### Option 1 — Docker (Recommended)

```bash
# Build the application JAR first
mvn package -DskipTests

# Start PostgreSQL + API together
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

---

### Option 2 — Local Development

**1. Start PostgreSQL**

```bash
# Start only the database container
docker-compose up postgres
```

Or configure a local PostgreSQL instance with:
| Setting  | Value          |
|----------|----------------|
| Database | `smarttask_db` |
| Username | `smarttask_user` |
| Password | `smarttask_pass` |
| Port     | `5432`         |

**2. Run the application**

```bash
mvn spring-boot:run
```

---

### Environment Variables

When running without Docker, the following properties can be overridden via environment variables:

| Variable                    | Default                                | Description          |
|-----------------------------|----------------------------------------|----------------------|
| `SPRING_DATASOURCE_URL`     | `jdbc:postgresql://localhost:5432/...` | Database URL         |
| `SPRING_DATASOURCE_USERNAME`| `smarttask_user`                       | DB username          |
| `SPRING_DATASOURCE_PASSWORD`| `smarttask_pass`                       | DB password          |
| `APP_JWT_SECRET`            | *(see application.properties)*         | JWT signing secret   |
| `APP_JWT_EXPIRATION`        | `86400000` (24 h)                      | Token TTL in ms      |

---

## API Endpoints

### Authentication

| Method | Path                   | Auth     | Description                          |
|--------|------------------------|----------|--------------------------------------|
| POST   | `/api/auth/register`   | None     | Register a new user (returns JWT)    |
| POST   | `/api/auth/login`      | None     | Login and receive a JWT token        |

### Tasks

| Method | Path                   | Auth     | Description                                          |
|--------|------------------------|----------|------------------------------------------------------|
| GET    | `/api/tasks`           | JWT      | Get all tasks (optional `?status=` / `?priority=`)   |
| POST   | `/api/tasks`           | JWT      | Create a new task                                    |
| GET    | `/api/tasks/stats`     | JWT      | Task counts grouped by status                        |
| GET    | `/api/tasks/{id}`      | JWT      | Get a specific task by ID                            |
| PUT    | `/api/tasks/{id}`      | JWT      | Update a task (partial — only non-null fields)       |
| DELETE | `/api/tasks/{id}`      | JWT      | Delete a task                                        |

### Users

| Method | Path                   | Auth          | Description                        |
|--------|------------------------|---------------|------------------------------------|
| GET    | `/api/users/me`        | JWT           | Get the current user's profile     |
| GET    | `/api/users`           | JWT + ADMIN   | List all users (admin only)        |

### Query Parameters

`GET /api/tasks` supports optional filtering:

| Parameter  | Values                        | Example                         |
|------------|-------------------------------|---------------------------------|
| `status`   | `TODO`, `IN_PROGRESS`, `DONE` | `GET /api/tasks?status=TODO`    |
| `priority` | `LOW`, `MEDIUM`, `HIGH`       | `GET /api/tasks?priority=HIGH`  |

---

## Request / Response Examples

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

### Create Task

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

### Error Response

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

**Security filter chain** (runs before the controller):

```
Request → JwtAuthFilter → SecurityFilterChain → Controller
              │
              └─ Extracts Bearer token → validates with JwtService
                 → loads UserDetails → sets SecurityContext
```

**Exception flow:**

```
Service throws RuntimeException
        │
        ▼
GlobalExceptionHandler (@RestControllerAdvice)
        │
        └─ Maps to ErrorResponse + HTTP status code
```

---

## Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests (fast, no Spring context)
mvn test -Dtest="TaskServiceTest,AuthServiceTest"

# Run only integration tests
mvn test -Dtest="TaskControllerTest"
```

Tests use **H2 in-memory database** (activated via the `test` Spring profile) so no running PostgreSQL is required.

| Test Class            | Type        | Coverage                                 |
|-----------------------|-------------|------------------------------------------|
| `TaskServiceTest`     | Unit        | createTask, getById, update, delete      |
| `AuthServiceTest`     | Unit        | register, login, duplicate validation    |
| `TaskControllerTest`  | Integration | All endpoints, status codes, JSON shape  |

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/api-docs
```

Click **Authorize** in the Swagger UI and paste your JWT token (without the `Bearer ` prefix) to authenticate all requests.

---

## Security Notes

- Passwords are hashed with **BCrypt** before storage — plain text is never persisted.
- JWT tokens are signed with **HMAC-SHA256** using the secret defined in `app.jwt.secret`.
- Replace the default JWT secret with a securely generated value before deploying to production.
- Tokens expire after **24 hours** by default (`app.jwt.expiration=86400000`).
- CSRF protection is disabled — appropriate for a stateless JWT API consumed by SPAs or mobile clients.
