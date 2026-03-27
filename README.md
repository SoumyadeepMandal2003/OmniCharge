\# ⚡ OmniCharge: Mobile Recharge \& Utility Payment Platform



!\[Spring Boot](https://img.shields.io/badge/Spring\_Boot-3.5.12-6DB33F?style=for-the-badge\&logo=spring-boot\&logoColor=white)

!\[Spring Cloud](https://img.shields.io/badge/Spring\_Cloud-Microservices-6DB33F?style=for-the-badge\&logo=spring\&logoColor=white)

!\[MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge\&logo=mysql\&logoColor=white)

!\[RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?style=for-the-badge\&logo=rabbitmq\&logoColor=white)

!\[Docker](https://img.shields.io/badge/Docker-Containerization-2496ED?style=for-the-badge\&logo=docker\&logoColor=white)



> A highly scalable, distributed microservices platform simulating a real-world telecom mobile recharge application. 



\---



\## 📖 1. Project Overview

\*\*OmniCharge\*\* is a robust digital backend system designed to allow users to perform mobile recharges and manage related transactions securely. Built entirely on a \*\*Microservices Architecture\*\*, this platform ensures high availability, independent scalability, and fault tolerance. 



The system encompasses modern backend engineering practices including \*\*Centralized JWT Authentication\*\*, \*\*Synchronous (OpenFeign)\*\* and \*\*Asynchronous (RabbitMQ)\*\* inter-service communication, distributed configuration, and full containerization via \*\*Docker\*\*.



\---



\## 🏗️ 2. High-Level Architecture



```text

Client (Web/Mobile/Postman)

&#x20;       │

&#x20;       ▼  (Authorization: Bearer <JWT>)

&#x20;┌───────────────────────────────────┐

&#x20;│        API Gateway (Port 8080)    │ ──▶ Centralized JWT Security \& Routing

&#x20;└─────────────────┬─────────────────┘

&#x20;                  │

&#x20;┌─────────────────┼─────────────────┬─────────────────┐

&#x20;│                 │                 │                 │

&#x20;▼                 ▼                 ▼                 ▼

User Service    Operator         Recharge           Payment 

(Port 8081)     Service          Service            Service

&#x20;               (Port 8082)      (Port 8083)        (Port 8084)

&#x20;│                 │                 │                 │

&#x20;└──▶ MySQL        └──▶ MySQL        ├──▶ MySQL        ├──▶ MySQL

&#x20;                                    │                 │

&#x20;                                    ▼                 ▼

&#x20;                             \[ RabbitMQ Message Broker ]

&#x20;                                          │

&#x20;                                          ▼

&#x20;                                Notification Service

&#x20;                                    (Port 8085)

```

\*Infrastructure Services: \*\*Eureka Service Registry\*\* (Port 8761) \& \*\*Spring Cloud Config Server\*\* (Port 8888)\*



\---



\## 🧩 3. Microservices Landscape



The system is broken down into 8 independently deployable components:



| Microservice | Port | Database | Primary Responsibility |

| :--- | :--- | :--- | :--- |

| \*\*Service Registry\*\* | `8761` | \*None\* | Netflix Eureka Server for dynamic service discovery. |

| \*\*Config Server\*\* | `8888` | \*None\* | Centralized configuration management linked to a private GitHub repo. |

| \*\*API Gateway\*\* | `8080` | \*None\* | Single entry point, routing, and centralized JWT validation (WebFlux). |

| \*\*User Service\*\* | `8081` | `omnicharge\_user` | User onboarding, authentication, and JWT token generation. |

| \*\*Operator Service\*\* | `8082` | `omnicharge\_operator` | Manages telecom operators (Jio, Airtel) and active recharge plans. |

| \*\*Recharge Service\*\* | `8083` | `omnicharge\_recharge` | Core business logic. Validates plans via OpenFeign and stores recharge history. |

| \*\*Payment Service\*\* | `8084` | `omnicharge\_payment` | Simulates secure payment processing and generates transaction UUIDs. |

| \*\*Notification Service\*\*| `8085` | \*None\* | Consumes async RabbitMQ events to trigger user alerts (SMS/Email simulation). |



\---



\## 💻 4. Technology Stack



\* \*\*Core Framework:\*\* Java 17, Spring Boot 3.5.x

\* \*\*Microservices Tools:\*\* Spring Cloud Netflix Eureka, Spring Cloud Gateway, Spring Cloud Config, OpenFeign

\* \*\*Security:\*\* Spring Security, JSON Web Tokens (JJWT)

\* \*\*Database \& ORM:\*\* MySQL 8.0, Spring Data JPA, Hibernate

\* \*\*Messaging System:\*\* RabbitMQ (Spring AMQP)

\* \*\*Testing:\*\* JUnit 5, Mockito

\* \*\*Documentation \& Monitoring:\*\* Swagger / OpenAPI 3.0, Spring Boot Actuator

\* \*\*DevOps:\*\* Docker, Docker Compose, Maven



\---



```text

📦 OmniCharge

│

├── 📁 service-registry

├── 📁 config-server

├── 📁 api-gateway

│

├── 📁 user-service

├── 📁 operator-service

├── 📁 recharge-service

├── 📁 payment-service

├── 📁 notification-service

│

├── 🐳 docker-compose.yml

└── 📄 README.md

```



\---



\## 🔐 5. Security Implementation



Authentication is implemented using \*\*JWT tokens\*\*.



\### Public APIs

```http

POST /auth/register

POST /auth/login

```



\### Protected APIs

Protected endpoints require a valid JWT token in the header. The JWT is generated in the \*\*user-service\*\* and validated at the API Gateway before accessing secured endpoints.

```http

Authorization: Bearer <jwt\_token>

```



\---



\## 🔄 6. Service Communication



\### Synchronous Communication (REST)

Using \*\*OpenFeign\*\*, the Recharge Service communicates synchronously with:

\- \*\*Operator Service\*\* → To validate the selected telecom plan.

\- \*\*Payment Service\*\* → To process the payment.



\### Asynchronous Communication (RabbitMQ)

The Recharge Service publishes a success event to decouple notifications:

\- \*\*Queue Name:\*\* `rechargeQueue`

\- The \*\*Notification Service\*\* listens to this event and processes it asynchronously.



\---



\## 🚀 7. How to Run (Deployment)



\### Prerequisites

\- Java 17 \& Maven

\- Docker \& Docker Desktop

\- MySQL \& RabbitMQ (if running locally without Docker)



\### Option A: The "One-Click" Docker Launch (Recommended)

The entire infrastructure and application stack is orchestrated via Docker Compose.



1\. Clone the repository:

&#x20;  ```bash

&#x20;  git clone \[https://github.com/your-username/OmniCharge.git](https://github.com/your-username/OmniCharge.git)

&#x20;  cd OmniCharge

&#x20;  ```

2\. Start the cluster:

&#x20;  ```bash

&#x20;  docker volume prune -f

&#x20;  docker-compose up -d --build

&#x20;  ```

3\. Wait \~60 seconds for Eureka and MySQL to initialize. Check the Eureka Dashboard at `http://localhost:8761`.



\### Option B: Local Manual Run Order

If running locally via your IDE, execute the following SQL commands first:

```sql

CREATE DATABASE omnicharge\_user;

CREATE DATABASE omnicharge\_operator;

CREATE DATABASE omnicharge\_recharge;

CREATE DATABASE omnicharge\_payment;

```

Then start the services in this exact sequence:

1\. `service-registry`

2\. `config-server`

3\. `mysql` \& `rabbitmq` (Ensure background services are running)

4\. `user-service`, `operator-service`, `payment-service`, `notification-service`

5\. `recharge-service`

6\. `api-gateway`



\---



\## 🛣️ 8. The "Golden Flow" (Testing the APIs)



All requests must go through the API Gateway: `http://localhost:8080`



\### 1. Register User

```http

POST /auth/register

```

```json

{

&#x20; "name": "Soumyadeep",

&#x20; "email": "soumyadeep@gmail.com",

&#x20; "password": "1234"

}

```



\### 2. Login \& Get JWT

```http

POST /auth/login

```

```json

{

&#x20; "email": "soumyadeep@gmail.com",

&#x20; "password": "1234"

}

```

\*Response will contain your \*\*JWT TOKEN\*\*. Copy this for the next step.\*



\### 3. Create Operator \& Plan

```http

POST /operators

POST /operators/plans

```



\### 4. Initiate Secure Recharge

```http

POST /recharge

Authorization: Bearer <token>

```

```json

{

&#x20; "mobileNumber": "9876543210",

&#x20; "operatorId": 1,

&#x20; "planId": 1

}

```



\---



\## 🔔 9. Event-Driven Flow



A successful recharge triggers an asynchronous event:



```text

Recharge Service  ──▶  RabbitMQ  ──▶  Notification Service

```

\*\*Console Output (Notification Service):\*\*

> `Recharge successful for 9876543210`



\---



\## 📊 10. Monitoring \& Docker Support



\*\*Actuator Endpoints:\*\*

Check the health and info of your services:

\* `/actuator/health`

\* `/actuator/info`



\*\*Docker Support:\*\*

Each microservice includes a `Dockerfile` for easy containerization. Example:

```dockerfile

FROM openjdk:17

COPY target/app.jar app.jar

ENTRYPOINT \["java","-jar","/app.jar"]

```



\---



\## 🧪 11. Unit Testing



Testing is implemented using \*\*JUnit 5\*\* and \*\*Mockito\*\*.

\* Example: `RechargeServiceTest`

\* Business logic is tested completely independently of the database and external APIs using isolated mocks.



\---



\## ⭐ 12. Key Highlights \& Learning Outcomes



\*\*Key Features:\*\*

\- ✔️ Microservices Architecture

\- ✔️ JWT Authentication \& API Gateway Routing

\- ✔️ Eureka Service Discovery

\- ✔️ OpenFeign Communication

\- ✔️ RabbitMQ Messaging

\- ✔️ Docker Ready \& JUnit Tested



\*\*Learning Outcomes:\*\*

\- Designing and implementing a robust distributed system.

\- Designing secure REST APIs and applying layered architecture principles.

\- Utilizing the Spring Cloud ecosystem for configuration and discovery.

\- Implementing event-driven, asynchronous communication.



\---



\## 👨‍💻 13. Developer Details



\*\*Developed by:\*\* Soumyadeep Mandal  

\*\*Role:\*\* Backend Developer / Microservices Architect  

\*\*Connect:\*\* \[LinkedIn Profile](#) | \[Portfolio](#)

