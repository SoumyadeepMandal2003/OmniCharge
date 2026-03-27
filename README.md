# ⚡ OmniCharge

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.12-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-Microservices-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**OmniCharge** is a dynamic, highly scalable, and distributed digital backend system built to simulate a real-world telecom mobile recharge and utility payment platform.  
Powered by a **Spring Boot Microservices Architecture**, it provides a robust ecosystem handling user authentication, operator plans, secure payments, and asynchronous event-driven notifications.

---

## 🚀 Features

- **🔐 Centralized Security:** JWT-based authentication validated entirely at the API Gateway level.
- **🌐 Dynamic Service Discovery:** Netflix Eureka ensures seamless and dynamic routing between microservices.
- **⚡ Synchronous Communication:** Smooth inter-service data fetching using **OpenFeign** (e.g., Recharge validating plans with Operator).
- **📬 Asynchronous Messaging:** Event-driven architecture using **RabbitMQ** to decouple payment success from notification delivery.
- **🐳 Fully Containerized:** Optimized for modern DevOps with a complete **Docker** and `docker-compose` setup for one-click deployment.
- **🛡️ Fault Tolerant & Isolated:** Every microservice maintains its own dedicated **MySQL** database, preventing single points of failure.
- **⚙️ Centralized Configuration:** Application properties are managed externally via Spring Cloud Config Server.

---

## 🛠️ Technologies Used

| Category | Technologies |
|-----------|---------------|
| **Core Framework** | Java 17, Spring Boot 3.5.x |
| **Microservices** | Spring Cloud (Gateway, Eureka, Config, OpenFeign) |
| **Security** | Spring Security, JSON Web Tokens (JJWT) |
| **Database & ORM** | MySQL 8.0, Spring Data JPA, Hibernate |
| **Messaging** | RabbitMQ (Spring AMQP) |
| **Testing** | JUnit 5, Mockito |
| **DevOps & Containerization**| Docker, Docker Compose, Maven |

---

## 🧩 Project Structure

The ecosystem is organized into independent microservices and infrastructure components:

```text
OmniCharge/
├── 📁 service-registry/       # Eureka Server for service discovery
├── 📁 config-server/          # Centralized configuration management
├── 📁 api-gateway/            # Single entry point & JWT validation
│
├── 📁 user-service/           # Auth, JWT generation, user management
├── 📁 operator-service/       # Telecom operators and plan details
├── 📁 recharge-service/       # Core business logic & OpenFeign orchestration
├── 📁 payment-service/        # Transaction simulation & UUID generation
├── 📁 notification-service/   # RabbitMQ consumer for user alerts
│
├── 🐳 docker-compose.yml      # Container orchestration
└── 📄 README.md
```

---

## ⚙️ Getting Started

Follow these steps to set up **OmniCharge** locally:

### 1️⃣ Clone the repository
```bash
git clone [https://github.com/soumyadeepmandal2003/OmniCharge.git](https://github.com/soumyadeepmandal2003/OmniCharge.git)
cd OmniCharge
```

### 2️⃣ Run via Docker (Recommended)
This project includes a fully configured `docker-compose.yml` that sets up all databases, RabbitMQ, Eureka, and the Spring Boot microservices automatically.

Clean any previous ghost volumes and start the cluster:
```bash
docker volume prune -f
docker-compose up -d --build
```
Your gateway should now be running on 👉 `http://localhost:8080` (Wait ~60 seconds for Eureka to register all services).

### 3️⃣ Run Manually (Local IDE Setup)
If running without Docker, you must spin up instances of MySQL and RabbitMQ, then start the services in this strict order:
1. `service-registry`
2. `config-server`
3. `user-service`, `operator-service`, `payment-service`, `notification-service`
4. `recharge-service`
5. `api-gateway`

---

## 🧠 How It Works (The Golden Flow)

1. **Authentication:** Users register and login via the API Gateway to the User Service, receiving a JWT token.
2. **Secure Access:** The user submits a recharge request to the API Gateway with the JWT in the `Authorization: Bearer` header.
3. **Validation:** The Gateway validates the token and routes the request to the Recharge Service.
4. **Orchestration:** The Recharge Service synchronously asks the Operator Service (via OpenFeign) if the plan is valid, then tells the Payment Service to process the transaction.
5. **Event Trigger:** Upon success, a message is dropped into RabbitMQ.
6. **Notification:** The Notification Service asynchronously picks up the message and logs a success alert.

---

## 🧑‍💻 Contributions

Contributions are always welcome and appreciated! ❤️  
Whether it’s adding a new microservice (like an Analytics service), improving CI/CD pipelines, or enhancing testing — every contribution helps **OmniCharge** grow.

### 🔧 Steps to Contribute

1. **Fork the Repository** Click on the **Fork** button at the top-right corner of this repository page.
2. **Clone Your Fork**
   ```bash
   git clone [https://github.com/](https://github.com/)<your-username>/OmniCharge.git
   cd OmniCharge
   ```
3. **Create a New Branch**
   ```bash
   git checkout -b feature-name
   ```
4. **Make Your Changes** Ensure your code is clean, well-formatted, and follows best practices for distributed systems.
5. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "Added: description of your feature or fix"
   ```
6. **Push to Your Branch**
   ```bash
   git push origin feature-name
   ```
7. **Open a Pull Request (PR)** Submit the PR for review with a clear title and description.

> “Good code tells a story — make sure yours is readable, meaningful, and easy to follow.” ✨

---

## 🐞 Troubleshooting

If you encounter issues while running or building the cluster, try the following:

1. 🔑 **401/403 Unauthorized Errors:** - Ensure you are passing the JWT token correctly in the header: `Authorization: Bearer <token>`.
   - Ensure the `jwt.secret` matches exactly between the `api-gateway` and `user-service` configurations.

2. 🐳 **Docker Port Conflicts:** - If a container fails to start, ensure ports `8080`, `3306`, `5672`, and `8761` are not being used by local instances of MySQL, RabbitMQ, or Tomcat on your host machine.

3. 🧹 **Ghost Data / Corrupted Database State:** - Docker volumes can cache old MySQL passwords or states. Wipe them completely with:  
     `docker-compose down -v` followed by `docker volume prune -f`.

4. 🧱 **Service Cannot Find Eureka:** - Microservices take time to register. If the API Gateway returns a `503 Service Unavailable`, wait 30-60 seconds and check `http://localhost:8761` to ensure the service is "UP".

---

> 💡 **Tip:** > If the issue persists, open a [GitHub Issue](https://github.com/soumyadeepmandal2003/OmniCharge/issues) describing your problem.  
> Include Docker logs (`docker logs <container-name>`) and steps to reproduce it!

---

## 🌟 Acknowledgements

- [**Spring Cloud**](https://spring.io/projects/spring-cloud) — for powerful microservices tooling.
- [**RabbitMQ**](https://www.rabbitmq.com/) — for robust asynchronous messaging.
- [**Docker**](https://www.docker.com/) — for making deployment a breeze.
- [**Mockito & JUnit**](https://site.mockito.org/) — for reliable unit testing frameworks.

---

## 💡 Author

**👨‍💻 Soumyadeep Mandal** [GitHub](https://github.com/soumyadeepmandal2003) • [LinkedIn](https://www.linkedin.com/in/soumyadeep2003/)  

> *“Architecting Scalability, One Service at a Time.”* 🌐