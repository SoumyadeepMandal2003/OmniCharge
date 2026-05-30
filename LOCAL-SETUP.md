# 🖥️ Running OmniCharge Locally

Complete guide to run the full OmniCharge stack on your local machine or laptop.

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Docker Desktop | 20+ | [docker.com](https://www.docker.com/products/docker-desktop) |
| Git | Any | [git-scm.com](https://git-scm.com) |
| Node.js | 20+ (for UI only) | [nodejs.org](https://nodejs.org) |

> **RAM:** 8GB minimum, 16GB recommended  
> **Disk:** 20GB free space

---

## Step 1 — Clone both repos

```bash
git clone https://github.com/SoumyadeepMandal2003/OmniCharge.git
git clone https://github.com/SoumyadeepMandal2003/OmniCharge-UI.git
```

---

## Step 2 — Start the backend (all services in Docker)

```bash
cd OmniCharge
docker compose -f docker-compose.full.yml up --build
```

First run downloads ~2-3GB of images and builds all 9 Spring Boot services.  
**Wait 3-5 minutes** for everything to start.

### Verify backend is up

Open these in your browser:

| URL | What you should see |
|-----|-------------------|
| http://localhost:8761 | Eureka dashboard — all 8 services registered |
| http://localhost:8080/actuator/health | `{"status":"UP"}` |
| http://localhost:8080/swagger-ui.html | Swagger UI with all APIs |
| http://localhost:15672 | RabbitMQ UI (login: `guest` / `guest`) |
| http://localhost:9411 | Zipkin tracing dashboard |

---

## Step 3 — Start the frontend

Open a **new terminal**:

```bash
cd OmniCharge-UI
npm install
npm start
```

Open **http://localhost:4200**

---

## Step 4 — Use the app

1. **Register** a new account at http://localhost:4200/auth/register
2. **Login** with your credentials
3. **Browse** operators and plans (auto-seeded on first startup)
4. **Recharge** any mobile number
5. **Check** history and transactions

### Create an Admin account

Register with `"role": "ADMIN"` to access the admin panel:
- Go to http://localhost:4200/auth/register
- Select **Admin** in the Account Type dropdown

---

## Stopping everything

```bash
# Stop all containers (keeps data)
docker compose -f docker-compose.full.yml down

# Stop and delete all data (fresh start)
docker compose -f docker-compose.full.yml down -v
```

---

## Sharing on local network (same WiFi)

Find your machine's local IP:

```bash
# Windows
ipconfig
# Look for: IPv4 Address . . . . . . . . . . : 192.168.x.x

# Mac / Linux
ifconfig | grep "inet "
```

Update `OmniCharge-UI/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://192.168.x.x:8080'   // ← your local IP
};
```

Restart the frontend (`npm start`). Anyone on the same WiFi can now access:
- Frontend: `http://192.168.x.x:4200`
- Swagger: `http://192.168.x.x:8080/swagger-ui.html`

---

## Troubleshooting

**Services not starting / crashing**
```bash
# Check logs for a specific service
docker logs omnicharge-auth
docker logs omnicharge-gateway
```

**Port already in use**
```bash
# Find what's using port 8080
# Windows:
netstat -ano | findstr :8080
# Mac/Linux:
lsof -i :8080
```

**Out of memory**
- Open Docker Desktop → Settings → Resources → increase Memory to at least 6GB

**Fresh start (wipe all data)**
```bash
docker compose -f docker-compose.full.yml down -v
docker system prune -f
docker compose -f docker-compose.full.yml up --build
```

---

## Port Reference

| Service | Port | URL |
|---------|------|-----|
| Angular UI | 4200 | http://localhost:4200 |
| API Gateway | 8080 | http://localhost:8080 |
| Eureka | 8761 | http://localhost:8761 |
| RabbitMQ UI | 15672 | http://localhost:15672 |
| Zipkin | 9411 | http://localhost:9411 |
| Auth Service | 8086 | http://localhost:8086 |
| User Service | 8081 | http://localhost:8081 |
| Recharge Service | 8082 | http://localhost:8082 |
| Payment Service | 8083 | http://localhost:8083 |
| Operator Service | 8084 | http://localhost:8084 |
| Notification Service | 8085 | http://localhost:8085 |
| MySQL | 3307 | localhost:3307 |
| RabbitMQ AMQP | 5672 | localhost:5672 |
