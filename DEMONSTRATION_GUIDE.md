# OmniCharge: Evaluator's Demonstration Guide

This guide provides a step-by-step walkthrough to demonstrate that all components of the OmniCharge platform are correctly implemented and integrated.

---

## Phase 1: Environment Setup & Health Check

### 1.1. Start the Platform
Run the following in the project root:
```bash
docker-compose up --build -d
```
*Wait ~90 seconds for all services to register.*

### 1.2. Verify Service Discovery
Open **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)
- **What to look for**: All 8 services (`API-GATEWAY`, `AUTH-SERVICE`, `USER-SERVICE`, `RECHARGE-SERVICE`, `PAYMENT-SERVICE`, `OPERATOR-SERVICE`, `NOTIFICATION-SERVICE`, `CONFIG-SERVER`) should be listed as **UP**.

---

## Phase 2: The Core Workflow (The "Golden Path")

We will now perform a complete end-to-end recharge flow.

### Step 1: User Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tester@omnicharge.com",
    "mobile": "9998887776",
    "password": "Password123",
    "fullName": "Test User"
  }'
```
- **Proof of Work**: This creates credentials in `auth-service` and a profile in `user-service` simultaneously via Feign.

### Step 2: Login & Obtain JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "tester@omnicharge.com", "password": "Password123"}'
```
- **Action**: Copy the `accessToken` from the response. We will use it as `<TOKEN>` in the next steps.

### Step 3: Admin Setup (Operators & Plans)
*Note: Operators and plans are **automatically seeded** on first startup by `operator-service`. You can skip straight to Step 4.*

To verify seeded data:
```bash
curl http://localhost:8080/api/operators
curl http://localhost:8080/api/plans
```

To create additional operators/plans (requires ADMIN token — register with `"role": "ADMIN"`):

**Create an Operator:**
```bash
curl -X POST http://localhost:8080/api/admin/operators \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Airtel", "code": "AT", "description": "Bharti Airtel"}'
```

### Step 4: Initiate Recharge
```bash
curl -X POST http://localhost:8080/api/recharges \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "mobileNumber": "9998887776",
    "operatorId": 1,
    "planId": 1
  }'
```
- **Internal Logic**: `recharge-service` calls `payment-service` (Feign). If payment is successful, it publishes an event to RabbitMQ.

---

## Phase 3: Verifying Advanced Integrations

### 3.1. Verify Asynchronous Notifications
Check the **Notification Service logs**:
```bash
docker logs omnicharge-notification
```
- **What to look for**: A log entry like `SMS -> [9998887776]: Dear Customer, Rs.499.00 recharge... is successful.` This proves **RabbitMQ** integration is working.

### 3.2. Verify Distributed Tracing
Open **Zipkin**: [http://localhost:9411](http://localhost:9411)
- Click "Run Query".
- Look for a trace starting from `api-gateway` that spans across `recharge-service`, `payment-service`, and `operator-service`.
- **Proof of Work**: This shows the entire microservices call chain is being tracked.

### 3.3. Verify Messaging Middleware
Open **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) (User: `guest`, Pass: `guest`)
- Check the **Exchanges** tab for `omnicharge.exchange`.
- Check the **Queues** tab for `recharge.completed.queue`.

### 3.4. Verify Multi-Database Persistence
Connect to the MySQL container:
```bash
docker exec -it omnicharge-mysql mysql -uomnicharge -pomnicharge123
```
Run:
```sql
USE omnicharge_recharge; SELECT * FROM recharges;
USE omnicharge_payments; SELECT * FROM transactions;
```

### 3.5. Verify Circuit Breaker (Fault Tolerance)
Check the circuit breaker health endpoint on recharge-service:
```bash
curl http://localhost:8082/actuator/health
```
- **What to look for**: `circuitBreakers` section showing `operator-service` and `payment-service` circuit breakers in `CLOSED` state (healthy).

To see circuit breaker metrics:
```bash
curl http://localhost:8082/actuator/circuitbreakers
```

---

## Phase 4: API Documentation
Show the evaluator the **aggregated Swagger UI** at the gateway — all 5 services in one page:
- **Gateway (all services)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Or per-service directly:
- User Service: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- Recharge Service: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- Auth Service: [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html)
