# Running OmniCharge on Low-End PC

## Memory Requirements

### Full Setup (All 9 Services)
- **Minimum**: 8GB RAM
- **Recommended**: 16GB RAM
- **Estimated Usage**: 4-6GB

### Minimal Setup (5 Core Services)
- **Minimum**: 4GB RAM
- **Recommended**: 6GB RAM
- **Estimated Usage**: 2-3GB

## Quick Start (Minimal Mode)

### Option 1: Automated Script (Easiest)
```bash
# Start minimal services
start-minimal.bat

# Stop all services
stop-all.bat
```

### Option 2: Manual Start (More Control)
```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Start services one by one (wait 15-20 seconds between each)
run-service.bat service-discovery
run-service.bat config-server
run-service.bat api-gateway
run-service.bat auth-service
run-service.bat user-service
```

## Service Startup Order (Important!)

Always start in this order:
1. **Docker Infrastructure** (MySQL, RabbitMQ, Zipkin)
2. **Service Discovery** (Eureka) - Wait until ready
3. **Config Server** - Wait until ready
4. **API Gateway**
5. **Auth Service**
6. **User Service**
7. Other services as needed

## Memory Optimization Tips

### 1. Reduce JVM Memory
Each service is configured to use:
- Initial heap: 128MB (`-Xms128m`)
- Maximum heap: 256MB (`-Xmx256m`)
- Metaspace: 128MB (`-XX:MaxMetaspaceSize=128m`)

### 2. Run Only What You Need
Don't start all services. Common scenarios:

**Testing Authentication:**
- service-discovery
- config-server
- api-gateway
- auth-service
- user-service

**Testing Recharge:**
- Above + recharge-service + operator-service

**Testing Payment:**
- Above + payment-service

**Testing Notifications:**
- Above + notification-service

### 3. Use Docker for Infrastructure Only
The current setup runs only MySQL, RabbitMQ, and Zipkin in Docker. Spring Boot services run locally with reduced memory.

### 4. Close Unnecessary Applications
- Close browser tabs
- Close IDEs when not editing
- Close other applications

## Troubleshooting

### Services Won't Start
**Problem**: "Port already in use"
**Solution**: 
```bash
# Check what's using the port
netstat -ano | findstr :8761

# Kill the process
taskkill /F /PID [process-id]
```

### Out of Memory Errors
**Problem**: `java.lang.OutOfMemoryError`
**Solution**:
1. Run fewer services
2. Increase swap/page file size in Windows
3. Close other applications

### Services Start But Don't Register
**Problem**: Services don't appear in Eureka dashboard
**Solution**:
1. Wait longer (can take 30-60 seconds)
2. Check service-discovery is running first
3. Check logs for connection errors

### Slow Performance
**Problem**: Everything is slow
**Solution**:
1. Reduce number of running services
2. Increase Windows page file size
3. Use SSD instead of HDD if possible
4. Disable unnecessary Windows services

## Service Ports Reference

| Service | Port | URL |
|---------|------|-----|
| Service Discovery | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| API Gateway | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |
| User Service | 8082 | http://localhost:8082 |
| Recharge Service | 8083 | http://localhost:8083 |
| Payment Service | 8084 | http://localhost:8084 |
| Operator Service | 8085 | http://localhost:8085 |
| Notification Service | 8086 | http://localhost:8086 |
| MySQL | 3307 | localhost:3307 |
| RabbitMQ | 5672, 15672 | http://localhost:15672 |
| Zipkin | 9411 | http://localhost:9411 |

## Development Workflow

### Typical Development Session
1. Start minimal services: `start-minimal.bat`
2. Work on specific service
3. Start additional services as needed: `run-service.bat [service-name]`
4. Stop all when done: `stop-all.bat`

### Testing a Single Service
```bash
# Start infrastructure
docker-compose up -d

# Start only the services you need
run-service.bat service-discovery
# Wait 20 seconds
run-service.bat config-server
# Wait 15 seconds
run-service.bat [your-service]
```

## Alternative: Use Pre-built JARs

If Maven is too slow, build JARs once and run them:

```bash
# Build all services once
mvn clean package -DskipTests

# Run from JAR (faster startup, less memory)
java -Xmx256m -Xms128m -jar auth-service/target/auth-service-1.0-SNAPSHOT.jar
```

## System Requirements Check

**Minimum System:**
- CPU: Dual-core 2.0GHz
- RAM: 4GB (with minimal setup)
- Storage: 5GB free space
- OS: Windows 10/11

**Recommended System:**
- CPU: Quad-core 2.5GHz+
- RAM: 8GB
- Storage: 10GB free space (SSD preferred)
- OS: Windows 10/11

## Questions?

If you're still having issues:
1. Check how much RAM you have: `systeminfo | findstr "Total Physical Memory"`
2. Check available RAM: Task Manager → Performance → Memory
3. Try running even fewer services
4. Consider using a cloud development environment for full testing
