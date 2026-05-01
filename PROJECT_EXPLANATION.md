# OmniCharge: Technical Deep Dive & Architecture Guide

This document provides a comprehensive overview of the **OmniCharge** platform, covering everything from core microservices architecture to advanced patterns like distributed tracing, asynchronous messaging, centralized security, and fault tolerance.

---

## 1. High-Level Architecture

OmniCharge is built using a **Microservices Architecture** pattern leveraging **Spring Cloud**. Each service is independent, has its own database, and communicates with others via REST (Synchronous) or Message Broker (Asynchronous).

### Core Components
| Service | Port | Role | Core Responsibility |
|---|---|---|---|
| **Service Discovery (Eureka)** | 8761 | Registry | Maintains a list of all live service instances. |
| **Config Server** | 8888 | Management | Centralized repository for all service configurations. |
| **API Gateway** | 8080 | Entry Point | Single entry point; handles routing, JWT validation, and Swagger aggregation. |
| **Auth Service** | 8086 | Identity | Manages user credentials, JWT issuance, refresh tokens, and session management. |
| **User Service** | 8081 | Profile | Manages user profiles and aggregates recharge/payment data for the dashboard. |
| **Recharge Service** | 8082 | Business Logic | Coordinates the mobile recharge workflow with circuit-breaker protection. |
| **Payment Service** | 8083 | Transactions | Handles payment processing and maintains transaction ledgers. |
| **Operator Service** | 8084 | Catalog | Manages telecom operators and recharge plans; auto-seeds data on startup. |
| **Notification Service** | 8085 | Communication | Asynchronously sends SMS/Email alerts via RabbitMQ events. |

---

## 2. Core Technical Aspects

### 2.1. Service Discovery (Netflix Eureka)
Instead of hardcoding service URLs, services register themselves with **Eureka**. When `recharge-service` needs to call `payment-service`, it looks up the location in Eureka. This allows for easy scaling (adding more instances) without configuration changes.

### 2.2. Centralized Configuration (Spring Cloud Config)
All services fetch their configuration from the **Config Server** at startup. This ensures consistency across the environment. A shared `application.yml` provides common settings (JWT secret, Jackson date format, logging) to all services, while per-service YAMLs override specifics.

### 2.3. Database per Service
To ensure loose coupling, every microservice has its own MySQL database:
- `omnicharge_auth`: User credentials and refresh tokens.
- `omnicharge_users`: Profile data (fullName, mobile, role).
- `omnicharge_recharge`: Recharge records and status tracking.
- `omnicharge_payments`: Transaction ledger.
- `omnicharge_operators`: Operator and plan catalog.

### 2.4. Automatic Data Seeding
`operator-service` seeds **4 real telecom operators** (Jio, Airtel, Vi, BSNL) and **11 recharge plans** on first startup via a `CommandLineRunner`. The seeder is idempotent — it checks for existing data before inserting.

---

## 3. Advanced Technical Aspects

### 3.1. Unified Security Model (JWT + Gateway + Internal Secret)
OmniCharge uses a **Stateful-at-Edge, Stateless-Downstream** security model:
1. **Authentication**: Users log in via `auth-service`, which returns an Access Token (JWT) and a Refresh Token.
2. **Validation**: The **API Gateway** intercepts every request. Its `JwtAuthenticationFilter` validates the JWT signature using the shared secret.
3. **Identity Propagation**: Once validated, the Gateway extracts user details (`userId`, `email`, `role`) and injects them into HTTP headers (`X-User-Id`, `X-User-Email`, `X-User-Role`) before forwarding to downstream services.
4. **Authorization**: Downstream services use these headers for identity — no redundant JWT re-validation.
5. **Internal Endpoint Security**: The `/api/users/internal/create` endpoint (called by auth-service to create user profiles) is protected by a shared `X-Internal-Secret` header, preventing external callers from bypassing auth-service.

### 3.2. Synchronous Communication (OpenFeign + Circuit Breaker)
For workflows requiring immediate results, OmniCharge uses **Spring Cloud OpenFeign** with **Resilience4j Circuit Breakers**:
- `recharge-service` calls `operator-service` (plan validation) and `payment-service` (payment processing).
- `user-service` calls `recharge-service` and `payment-service` for dashboard aggregation.
- If a downstream service is unavailable, the circuit breaker opens and a **fallback** is triggered — returning a graceful error instead of cascading failures.
- Circuit breaker state is visible at `/actuator/health` and `/actuator/circuitbreakers`.

### 3.3. Asynchronous Messaging (RabbitMQ)
For non-blocking tasks, the system uses **Event-Driven Architecture**:
- **Exchange**: `omnicharge.exchange` (Topic)
- **Queue**: `recharge.completed.queue`
- **Routing Key**: `recharge.completed`
- After a successful recharge, `recharge-service` publishes a `RechargeResponse` event. The RabbitMQ publish is wrapped in a try-catch — a messaging failure does **not** roll back the recharge.
- `notification-service` consumes the event and sends SMS/Email confirmations (simulated via logging, pluggable with Twilio/AWS SNS).

### 3.4. Distributed Tracing (Zipkin)
Every request is assigned a unique `Trace ID` that follows it across all services via **Micrometer + Brave**. The Zipkin UI at `http://localhost:9411` visualizes the entire call chain, making it easy to identify bottlenecks.

### 3.5. Fault Tolerance & Resilience
- **Circuit Breakers**: Resilience4j protects all inter-service Feign calls with configurable failure thresholds (50% failure rate over 10 calls opens the circuit for 10 seconds).
- **Time Limiters**: Feign calls time out after 3–5 seconds to prevent thread starvation.
- **Fallbacks**: Each Feign client has a fallback implementation that logs the failure and throws a user-friendly exception.
- **Health Checks**: Docker Compose health checks for MySQL, RabbitMQ, and auth-service ensure services start in the correct order.
- **RabbitMQ Fault Isolation**: Notification failures are isolated — a RabbitMQ outage does not affect recharge success.

### 3.6. API Documentation (Aggregated Swagger)
All 5 business services are documented via **SpringDoc OpenAPI 3**. The API Gateway aggregates all service docs into a **single Swagger UI** at `http://localhost:8080/swagger-ui.html`, with a dropdown to switch between services.

---

## 4. Key Business Workflows

### 4.1. Registration Flow
1. Client POSTs to `/api/auth/register`.
2. `auth-service` saves credentials (BCrypt-hashed password) to `omnicharge_auth`.
3. `auth-service` calls `user-service` via Feign (with `X-Internal-Secret` header) to create the profile in `omnicharge_users`.
4. Returns JWT access token + refresh token.

### 4.2. The Recharge Workflow
1. Client sends `POST /api/recharges` with JWT.
2. **Gateway** validates JWT, injects user headers, routes to `recharge-service`.
3. **Recharge Service**:
   - Calls `operator-service` via Feign to fetch operator and plan details (circuit-breaker protected).
   - Validates plan belongs to the specified operator.
   - Creates a `PENDING` recharge record.
   - Calls `payment-service` via Feign to process payment (circuit-breaker protected).
4. **Payment Service** simulates gateway processing, saves transaction as `SUCCESS`.
5. **Recharge Service** updates status to `SUCCESS`, saves `transactionId`.
6. Publishes `recharge.completed` event to RabbitMQ (non-blocking, fault-isolated).
7. **Notification Service** consumes event, logs SMS/Email confirmation.

### 4.3. Token Refresh Flow
1. Client POSTs to `/api/auth/refresh` with the refresh token.
2. `auth-service` validates the token is not revoked or expired.
3. Revokes the old refresh token (rotation) and issues a new access + refresh token pair.

---

## 5. Testing Strategy

| Layer | Tool | Coverage |
|---|---|---|
| Unit (Service) | JUnit 5 + Mockito | All 5 business services |
| Unit (Controller) | @WebMvcTest + MockMvc | auth, user, recharge, payment, operator |
| In-Memory DB | H2 | All services with JPA |
| Security | spring-security-test | @WithMockUser for role-based tests |

Tests are isolated from external dependencies — Eureka is disabled, H2 replaces MySQL, and Feign clients are mocked.

---

## 6. CI/CD Pipeline (GitHub Actions)

The `.github/workflows/ci.yml` pipeline:
1. **Build & Test**: Runs `mvn clean verify` for all 6 services in parallel using a matrix strategy.
2. **Upload Reports**: Saves Surefire test reports as artifacts on every run.
3. **Docker Build**: On pushes to `main`, builds Docker images for all 9 services and verifies `docker-compose config`.

---

## 7. Environment Variables Reference

| Variable | Used By | Description |
|---|---|---|
| `JWT_SECRET` | All services | JWT signing key |
| `INTERNAL_SECRET` | auth-service, user-service | Shared secret for internal endpoint protection |
| `SPRING_DATASOURCE_URL` | DB services | MySQL connection URL |
| `RABBITMQ_HOST` | recharge, payment, notification | RabbitMQ hostname |
| `RABBITMQ_USERNAME` | recharge, payment, notification | RabbitMQ username |
| `RABBITMQ_PASSWORD` | recharge, payment, notification | RabbitMQ password |
| `EUREKA_URI` | All services | Eureka server URL |
| `ZIPKIN_URL` | All services | Zipkin tracing endpoint |
| `CONFIG_SERVER_URL` | All services | Config server URL |
| `PAYMENT_MAX_AMOUNT` | payment-service | Maximum allowed transaction amount |
