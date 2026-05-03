<div align="center">

<img src="https://img.shields.io/badge/OmniCharge-Mobile%20Recharge%20Platform-6366f1?style=for-the-badge&logo=lightning&logoColor=white" alt="OmniCharge"/>

# <img src="https://github.com/SoumyadeepMandal2003/OmniCharge/blob/main/OmniCharge_created_icon.ico" width="40" height="40" style="vertical-align:middle"/> OmniCharge

### *Production-grade Microservices Backend for Mobile Recharge*

[![Java](https://img.shields.io/badge/Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud%202023-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![MySQL](https://img.shields.io/badge/MySQL%208-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)

<br/>

> **9 microservices. 1 gateway. Zero compromise.**  
> Built with Spring Boot 3, Spring Cloud, JWT auth, RabbitMQ async messaging,  
> Resilience4j circuit breakers, and deployed on Azure VM via GitHub Actions CI/CD.

<br/>

[![Live API](https://img.shields.io/badge/🌐%20Live%20API-4.186.25.145:8080-10b981?style=for-the-badge)](http://4.186.25.145:8080/actuator/health)
[![Swagger UI](https://img.shields.io/badge/📖%20Swagger%20UI-Explore%20APIs-6366f1?style=for-the-badge)](http://4.186.25.145:8080/swagger-ui.html)
[![Eureka](https://img.shields.io/badge/🔍%20Eureka%20Dashboard-Service%20Registry-f59e0b?style=for-the-badge)](http://4.186.25.145:8761)

</div>

---

## 📋 Table of Contents

- [✨ Features](#-features)
- [🏗️ Architecture](#️-architecture)
- [🧩 Services](#-services)
- [🔐 Security](#-security)
- [🚀 Quick Start](#-quick-start)
- [⚙️ Local Development](#️-local-development)
- [📡 API Reference](#-api-reference)
- [🧪 Testing](#-testing)
- [📊 Monitoring](#-monitoring)
- [🌍 Deployment](#-deployment)
- [🛠️ Tech Stack](#️-tech-stack)
- [📁 Project Structure](#-project-structure)

---

## ✨ Features

<table>
<tr>
<td>

**🔐 Auth & Security**
- JWT access + refresh token flow
- Role-based access control (`USER` / `ADMIN`)
- Password change with session revocation
- Shared internal secret for service-to-service calls
- Full account deletion cascade

</td>
<td>

**📱 Core Business**
- Mobile recharge initiation & history
- Multi-operator & multi-plan support
- Real-time payment processing simulation
- Transaction ledger with full audit trail
- Async notifications via RabbitMQ

</td>
</tr>
<tr>
<td>

**🏛️ Infrastructure**
- Netflix Eureka service discovery
- Spring Cloud Config Server (centralized config)
- Spring Cloud Gateway with JWT filter
- Resilience4j circuit breakers + fallbacks
- Distributed tracing with Zipkin

</td>
<td>

**🚢 DevOps**
- Full Docker Compose setup (dev + prod)
- GitHub Actions CI/CD → Azure VM auto-deploy
- Per-service health checks via Actuator
- Aggregated Swagger UI at gateway
- H2 in-memory DB for isolated unit tests

</td>
</tr>
</table>

---

## 🏗️ Architecture

```
                          ┌─────────────────────────────────────────────┐
                          │              CLIENT (Angular UI)             │
                          └──────────────────────┬──────────────────────┘
                                                 │ HTTP
                          ┌──────────────────────▼──────────────────────┐
                          │         🌐 API GATEWAY  :8080               │
                          │    Spring Cloud Gateway + JWT Filter         │
                          └──┬──────┬──────┬──────┬──────┬──────┬───────┘
                             │      │      │      │      │      │
              ┌──────────────▼─┐ ┌──▼───┐ ┌──▼───┐ ┌──▼───┐ ┌──▼──────────┐
              │ 🔐 auth-service│ │👤user│ │📱rchrg│ │💳 pay│ │📡 operator  │
              │     :8086      │ │:8081 │ │ :8082 │ │:8083 │ │   :8084     │
              └────────────────┘ └──────┘ └───┬───┘ └──────┘ └─────────────┘
                                              │ RabbitMQ
                                         ┌────▼────────────┐
                                         │ 🔔 notification │
                                         │    :8085        │
                                         └─────────────────┘

              ┌─────────────────────────────────────────────────────────┐
              │                   INFRASTRUCTURE                        │
              │  🗄️ MySQL :3306  🐰 RabbitMQ :5672  🔍 Eureka :8761   │
              │  ⚙️ Config :8888              🔭 Zipkin :9411           │
              └─────────────────────────────────────────────────────────┘
```

---

## 🧩 Services

| # | Service | Port | Role |
|---|---------|------|------|
| 1 | 🔍 **service-discovery** | `8761` | Netflix Eureka — service registry |
| 2 | ⚙️ **config-server** | `8888` | Centralized config (native classpath) |
| 3 | 🌐 **api-gateway** | `8080` | Single entry point, JWT validation, routing |
| 4 | 🔐 **auth-service** | `8086` | Registration, login, JWT issuance, refresh tokens |
| 5 | 👤 **user-service** | `8081` | User profiles, recharge & transaction dashboard |
| 6 | 📱 **recharge-service** | `8082` | Recharge initiation, history, status |
| 7 | 💳 **payment-service** | `8083` | Transaction processing & ledger |
| 8 | 📡 **operator-service** | `8084` | Telecom operators & plans catalog |
| 9 | 🔔 **notification-service** | `8085` | Async RabbitMQ consumer for events |

> Each service has its **own MySQL database** — fully decoupled. Cross-service communication uses **Feign clients** with circuit breaker fallbacks.

---

## 🔐 Security

```
┌─────────────────────────────────────────────────────────────┐
│                     AUTH FLOW                               │
│                                                             │
│  Register/Login ──► accessToken (24h) + refreshToken (7d)  │
│                                                             │
│  Every Request ──► Authorization: Bearer <accessToken>     │
│                                                             │
│  Token Expired ──► POST /api/auth/refresh                  │
│                                                             │
│  Logout ──► Refresh token revoked (soft delete)            │
│                                                             │
│  Delete Account ──► All data purged across all services    │
└─────────────────────────────────────────────────────────────┘
```

**Roles:**
- `USER` — recharge, view own history, manage profile
- `ADMIN` — everything above + manage operators, plans, view all users/transactions

**Internal Security:**  
Service-to-service calls (e.g. auth → user) use a shared `X-Internal-Secret` header to prevent external callers from hitting internal endpoints directly.

---

## 🚀 Quick Start

### Prerequisites

```bash
docker --version   # 20+
docker compose version  # 2+
```

### One-command startup

```bash
git clone https://github.com/SoumyadeepMandal2003/OmniCharge.git
cd OmniCharge
cp .env.example .env        # fill in your secrets
docker compose -f docker-compose.full.yml up --build
```

⏳ Allow **~90 seconds** on first run for all services to register with Eureka.

### Verify everything is up

```bash
# All services should show "UP"
curl http://localhost:8080/actuator/health

# Check Eureka — all 8 services should be registered
open http://localhost:8761
```

---

## ⚙️ Local Development

### Prerequisites

- ☕ Java 17+
- 📦 Maven 3.8+
- 🗄️ MySQL 8.0 on `localhost:3306`
- 🐰 RabbitMQ on `localhost:5672`

### Database Setup

```sql
CREATE DATABASE omnicharge_auth;
CREATE DATABASE omnicharge_users;
CREATE DATABASE omnicharge_recharge;
CREATE DATABASE omnicharge_payments;
CREATE DATABASE omnicharge_operators;

CREATE USER 'omnicharge'@'%' IDENTIFIED BY 'omnicharge123';
GRANT ALL PRIVILEGES ON omnicharge_*.* TO 'omnicharge'@'%';
FLUSH PRIVILEGES;
```

### Start Services (in order)

```bash
# 1. Infrastructure first
cd service-discovery   && mvn spring-boot:run &
cd config-server       && mvn spring-boot:run &

# 2. Gateway
cd api-gateway         && mvn spring-boot:run &

# 3. Business services (any order)
cd auth-service        && mvn spring-boot:run &
cd user-service        && mvn spring-boot:run &
cd operator-service    && mvn spring-boot:run &
cd recharge-service    && mvn spring-boot:run &
cd payment-service     && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
```

---

## 📡 API Reference

> **Base URL:** `http://4.186.25.145:8080`  
> **Full Swagger UI:** [`/swagger-ui.html`](http://4.186.25.145:8080/swagger-ui.html)

### 🔐 Auth Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/register` | ❌ | Register new user |
| `POST` | `/api/auth/login` | ❌ | Login → get tokens |
| `POST` | `/api/auth/refresh` | ❌ | Refresh access token |
| `GET`  | `/api/auth/validate` | ❌ | Validate JWT (internal) |
| `PUT`  | `/api/auth/password` | ✅ | Change password |
| `POST` | `/api/auth/logout` | ✅ | Logout current device |
| `POST` | `/api/auth/logout-all` | ✅ | Logout all devices |
| `DELETE` | `/api/auth/account` | ✅ | Permanently delete account |

### 👤 User Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET`  | `/api/users/me` | ✅ | Get my profile |
| `PUT`  | `/api/users/me` | ✅ | Update my profile |
| `GET`  | `/api/users/{id}` | ✅ | Get user by ID |
| `GET`  | `/api/admin/users` | 👑 | Get all users |
| `GET`  | `/api/users/me/recharges` | ✅ | My recharge history |
| `GET`  | `/api/users/me/transactions` | ✅ | My transactions |

### 📡 Operator & Plans

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET`  | `/api/operators` | ❌ | All active operators |
| `GET`  | `/api/plans` | ❌ | All active plans |
| `GET`  | `/api/operators/{id}/plans` | ❌ | Plans by operator |
| `POST` | `/api/admin/operators` | 👑 | Create operator |
| `PUT`  | `/api/admin/operators/{id}` | 👑 | Update operator |
| `DELETE` | `/api/admin/operators/{id}` | 👑 | Deactivate operator |
| `POST` | `/api/admin/plans` | 👑 | Create plan |
| `DELETE` | `/api/admin/plans/{id}` | 👑 | Deactivate plan |

### 📱 Recharge Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/recharges` | ✅ | Initiate recharge |
| `GET`  | `/api/recharges/history` | ✅ | My recharge history |
| `GET`  | `/api/recharges/{rechargeId}` | ✅ | Get by recharge ID |

### 💳 Payment Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET`  | `/api/payments/transaction/{id}` | ✅ | Get transaction |
| `GET`  | `/api/payments/recharge/{id}` | ✅ | Transaction by recharge |
| `GET`  | `/api/payments/user/{userId}` | ✅ | User transactions |
| `GET`  | `/api/payments/admin/all` | 👑 | All transactions |

> ✅ = Bearer token required &nbsp;&nbsp; 👑 = ADMIN role required &nbsp;&nbsp; ❌ = Public

### Quick Example

```bash
# 1. Register
curl -X POST http://4.186.25.145:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"Test@1234","fullName":"John Doe","mobile":"9876543210"}'

# 2. Login → grab accessToken
TOKEN=$(curl -s -X POST http://4.186.25.145:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"Test@1234"}' | jq -r '.accessToken')

# 3. Recharge
curl -X POST http://4.186.25.145:8080/api/recharges \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"mobileNumber":"9876543210","operatorId":1,"planId":1}'
```

---

## 🧪 Testing

### Unit & Integration Tests

```bash
# Run tests for all services
for svc in auth-service user-service recharge-service payment-service operator-service; do
  echo "🧪 Testing $svc..."
  cd $svc && mvn test && cd ..
done
```

**Test coverage includes:**
- ✅ Service layer unit tests (Mockito)
- ✅ Controller slice tests (`@WebMvcTest`)
- ✅ H2 in-memory DB for isolated test runs
- ✅ Spring Security test with `@WithMockUser`

### Postman Collection

Import `OmniCharge.postman_collection.json` — **56 requests** covering:

```
Phase 1 — Auth Setup & Verification    (01–14)
Phase 2 — User Profile Management      (15–21)
Phase 3 — Operators & Plans            (22–33)
Phase 4 — Recharge Flow                (34–39)
Phase 5 — Payment Transactions         (40–44)
Phase 6 — Dashboard Aggregation        (45–49)
Phase 7 — Health Checks                (50–56)
```

> Negative test cases (wrong password, invalid mobile, unauthorized access) are included and expected to return error codes.

---

## 📊 Monitoring

| Dashboard | URL | Credentials |
|-----------|-----|-------------|
| 🔍 **Eureka** — Service Registry | [`http://4.186.25.145:8761`](http://4.186.25.145:8761) | None |
| 🐰 **RabbitMQ** — Message Broker | [`http://4.186.25.145:15672`](http://4.186.25.145:15672) | `omnicharge` / `OmniRabbit@2024` |
| 🔭 **Zipkin** — Distributed Tracing | [`http://4.186.25.145:9411`](http://4.186.25.145:9411) | None |
| 📖 **Swagger UI** — API Docs | [`http://4.186.25.145:8080/swagger-ui.html`](http://4.186.25.145:8080/swagger-ui.html) | JWT token |

### Health Checks

```bash
# Gateway (all services)
curl http://4.186.25.145:8080/actuator/health

# Individual services
curl http://4.186.25.145:8086/actuator/health  # auth
curl http://4.186.25.145:8081/actuator/health  # user
curl http://4.186.25.145:8082/actuator/health  # recharge
curl http://4.186.25.145:8083/actuator/health  # payment
curl http://4.186.25.145:8084/actuator/health  # operator
```

---

## 🌍 Deployment

### Azure VM (Production)

Deployed on **Microsoft Azure Standard_B2s** (2 vCPU, 4GB RAM) via Docker Compose.

```bash
# SSH into VM
ssh -i ~/.ssh/omnicharge_azure azureuser@4.186.25.145

# Redeploy all services
cd OmniCharge
./deploy/redeploy.sh

# Redeploy single service
./deploy/redeploy.sh auth-service
```

### CI/CD — GitHub Actions

Every push to `main` automatically:

```
git push origin main
       │
       ▼
GitHub Actions triggered
       │
       ▼
SSH into Azure VM
       │
       ▼
git pull origin main
       │
       ▼
docker compose build --parallel
       │
       ▼
docker compose up -d --force-recreate
       │
       ▼
✅ All services live in ~10 minutes
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password |
| `MYSQL_USER` | DB username |
| `MYSQL_PASSWORD` | DB password |
| `JWT_SECRET` | JWT signing key (min 32 chars) |
| `INTERNAL_SECRET` | Service-to-service shared secret |
| `RABBITMQ_USER` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | RabbitMQ password |

```bash
cp .env.example .env
nano .env  # fill in your values
```

---

## 🛠️ Tech Stack

<table>
<tr><td><strong>Category</strong></td><td><strong>Technology</strong></td></tr>
<tr><td>🏗️ Framework</td><td>Spring Boot 3.2 · Spring Cloud 2023</td></tr>
<tr><td>🔐 Security</td><td>Spring Security · JJWT 0.11.5 · BCrypt</td></tr>
<tr><td>🗄️ Database</td><td>MySQL 8.0 · Spring Data JPA · Hibernate</td></tr>
<tr><td>📨 Messaging</td><td>RabbitMQ · Spring AMQP</td></tr>
<tr><td>🔍 Discovery</td><td>Netflix Eureka (Spring Cloud)</td></tr>
<tr><td>🌐 Gateway</td><td>Spring Cloud Gateway (reactive)</td></tr>
<tr><td>🔗 Inter-service</td><td>OpenFeign · Resilience4j Circuit Breaker</td></tr>
<tr><td>📖 API Docs</td><td>SpringDoc OpenAPI 3 · Swagger UI (aggregated)</td></tr>
<tr><td>🔭 Tracing</td><td>Micrometer · Zipkin</td></tr>
<tr><td>🧪 Testing</td><td>JUnit 5 · Mockito · H2 · Spring Security Test</td></tr>
<tr><td>🐳 Container</td><td>Docker · Docker Compose</td></tr>
<tr><td>🚀 CI/CD</td><td>GitHub Actions → Azure VM</td></tr>
<tr><td>☁️ Cloud</td><td>Microsoft Azure (Standard_B2s)</td></tr>
<tr><td>🔨 Build</td><td>Maven 3.8+</td></tr>
</table>

---

## 📁 Project Structure

```
OmniCharge/
├── 🌐 api-gateway/              # Spring Cloud Gateway + JWT filter
├── 🔐 auth-service/             # Auth, JWT, refresh tokens
├── 👤 user-service/             # User profiles + dashboard aggregation
├── 📱 recharge-service/         # Recharge workflow
├── 💳 payment-service/          # Transaction processing
├── 📡 operator-service/         # Operators & plans catalog
├── 🔔 notification-service/     # Async RabbitMQ consumer
├── 🔍 service-discovery/        # Netflix Eureka registry
├── ⚙️  config-server/            # Centralized config server
│   └── src/main/resources/
│       └── config/              # Per-service YAML configs
├── 🚀 deploy/
│   ├── azure-setup.sh           # One-time VM setup script
│   ├── redeploy.sh              # Redeploy script (all or single service)
│   └── AZURE-GUIDE.md           # Step-by-step Azure deployment guide
├── 🐳 docker-compose.yml        # Dev (infra only)
├── 🐳 docker-compose.full.yml   # Full local Docker setup
├── 🐳 docker-compose.prod.yml   # Production (Azure)
├── 🗄️  init.sql                  # MySQL database initialization
├── 📮 OmniCharge.postman_collection.json
└── 📖 API-DOCUMENTATION.md      # Full API reference
```

---

<div align="center">

**Built with ❤️ using Spring Boot & Spring Cloud**

[![GitHub](https://img.shields.io/badge/GitHub-SoumyadeepMandal2003-181717?style=for-the-badge&logo=github)](https://github.com/SoumyadeepMandal2003/OmniCharge)

*If this project helped you, drop a ⭐ — it means a lot!*

</div>
