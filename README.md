# OmniCharge — Mobile Recharge & Utility Payment Platform

A production-style microservices backend built with Spring Boot 3, Spring Cloud, RabbitMQ, and MySQL.

---

## Architecture

| Service | Port | Description |
|---|---|---|
| service-discovery | 8761 | Eureka service registry |
| config-server | 8888 | Centralised config (native) |
| api-gateway | 8080 | Spring Cloud Gateway + JWT filter |
| auth-service | 8086 | Credentials, JWT issuance, refresh tokens |
| user-service | 8081 | Registration, profile management |
| recharge-service | 8082 | Recharge initiation & history |
| payment-service | 8083 | Transaction processing |
| operator-service | 8084 | Operators & plans management |
| notification-service | 8085 | Async RabbitMQ consumer |

---

## Quick Start (Docker Compose)

### Prerequisites
- Docker 20+
- Docker Compose 2+

### Run everything

```bash
# Clone / unzip project
cd omnicharge

# Start all services
docker-compose up --build

# Or run in background
docker-compose up --build -d
```

### Startup order (handled automatically by depends_on)
1. MySQL + RabbitMQ (infrastructure)
2. service-discovery (Eureka)
3. config-server
4. api-gateway + all microservices

Allow ~90 seconds on first run for all services to register with Eureka.

---

## Running Locally (without Docker)

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0 running on localhost:3306
- RabbitMQ running on localhost:5672

### Setup MySQL
```sql
CREATE DATABASE omnicharge_users;
CREATE DATABASE omnicharge_recharge;
CREATE DATABASE omnicharge_payments;
CREATE DATABASE omnicharge_operators;
CREATE DATABASE omnicharge_auth;
CREATE USER 'omnicharge'@'%' IDENTIFIED BY 'omnicharge123';
GRANT ALL PRIVILEGES ON omnicharge_*.* TO 'omnicharge'@'%';
FLUSH PRIVILEGES;
```

### Start each service
```bash
# Start in this order:
cd service-discovery  && mvn spring-boot:run &
cd config-server      && mvn spring-boot:run &
cd api-gateway        && mvn spring-boot:run &
cd user-service       && mvn spring-boot:run &
cd operator-service   && mvn spring-boot:run &
cd recharge-service   && mvn spring-boot:run &
cd payment-service    && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
```

---

## API Usage

All requests go through the **API Gateway on port 8080**.

### 1. Register a user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "mobile": "9876543210",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

### 2. Login and get JWT token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'
```
Copy the `token` from the response.

### 3. Create an operator (Admin only)
```bash
curl -X POST http://localhost:8080/api/admin/operators \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Airtel", "code": "AT", "description": "Airtel Telecom"}'
```

### 4. Create a plan
```bash
curl -X POST http://localhost:8080/api/admin/plans \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "299 Plan",
    "price": 299.00,
    "validityDays": 28,
    "data": "1.5GB/day",
    "calls": "Unlimited",
    "sms": "100/day",
    "type": "PREPAID",
    "operatorId": 1
  }'
```

### 5. Initiate a recharge
```bash
curl -X POST http://localhost:8080/api/recharges \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "mobileNumber": "9876543210",
    "operatorId": 1,
    "planId": 1
  }'
```

### 6. View recharge history
```bash
curl http://localhost:8080/api/recharges/history \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Swagger UI

All services are accessible through the **API Gateway Swagger UI** on a single page:

- **Aggregated (all services)**: http://localhost:8080/swagger-ui.html

Or directly per service:

| Service | Swagger URL |
|---|---|
| auth-service | http://localhost:8086/swagger-ui.html |
| user-service | http://localhost:8081/swagger-ui.html |
| recharge-service | http://localhost:8082/swagger-ui.html |
| payment-service | http://localhost:8083/swagger-ui.html |
| operator-service | http://localhost:8084/swagger-ui.html |

---

## Running Tests

```bash
# Run all tests across all services
for svc in user-service recharge-service payment-service operator-service notification-service; do
  echo "Testing $svc..."
  cd $svc && mvn test && cd ..
done
```

---

## Monitoring

- Eureka Dashboard:       http://localhost:8761
- RabbitMQ Management:    http://localhost:15672 (guest/guest)
- Actuator health (each): http://localhost:808X/actuator/health

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| SPRING_DATASOURCE_URL | jdbc:mysql://localhost:3306/... | MySQL URL |
| SPRING_DATASOURCE_USERNAME | omnicharge | DB username |
| SPRING_DATASOURCE_PASSWORD | omnicharge123 | DB password |
| JWT_SECRET | OmniChargeSecretKey... | JWT signing key |
| EUREKA_URI | http://localhost:8761/eureka | Eureka server URL |
| RABBITMQ_HOST | localhost | RabbitMQ host |

---

## Technology Stack

- **Framework**: Spring Boot 3.2, Spring Cloud 2023
- **Security**: Spring Security + JWT (jjwt 0.11.5)
- **Database**: MySQL 8 + Hibernate ORM (JPA)
- **Messaging**: RabbitMQ (Spring AMQP)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Fault Tolerance**: Resilience4j Circuit Breaker
- **Documentation**: SpringDoc OpenAPI 3 / Swagger UI (aggregated at gateway)
- **Testing**: JUnit 5, Mockito, H2 (in-memory for tests)
- **Containerisation**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **Build**: Maven
