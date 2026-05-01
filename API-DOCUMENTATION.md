# OmniCharge API Documentation

**Base URL**: `http://4.186.25.145:8080`  
**Version**: 1.0.0  
**Auth**: JWT Bearer Token

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [Auth Service APIs](#auth-service-apis)
4. [User Service APIs](#user-service-apis)
5. [Operator Service APIs](#operator-service-apis)
6. [Recharge Service APIs](#recharge-service-apis)
7. [Payment Service APIs](#payment-service-apis)
8. [Health Check APIs](#health-check-apis)
9. [Error Reference](#error-reference)
10. [Testing Guide](#testing-guide)

---

## Getting Started

### Prerequisites
- Postman (download from https://www.postman.com)
- Import `OmniCharge.postman_collection.json` from the project root

### How Authentication Works
```
1. Register or Login → get accessToken + refreshToken
2. Add accessToken to every protected request as:
   Header: Authorization: Bearer <accessToken>
3. accessToken expires in 24 hours → use refreshToken to get a new one
4. refreshToken expires in 7 days → login again after that
```

### Quick Start Flow
```
Register → Login → Create Operator (Admin) → Create Plan (Admin) → Initiate Recharge → View History
```

---

## Authentication

All protected endpoints require this header:
```
Authorization: Bearer <your_access_token>
```

**Roles:**
- `USER` — regular user, can recharge and view own data
- `ADMIN` — can manage operators, plans, and view all data

---

## Auth Service APIs

**Base path**: `/api/auth`  
**Port**: 8086 (accessed via gateway at 8080)

---

### POST /api/auth/register

Register a new user account.

**Auth required**: No

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "Test@1234",
  "fullName": "John Doe",
  "mobile": "9876543210",
  "role": "USER"
}
```

**Field Rules**:
| Field | Required | Rules |
|---|---|---|
| email | Yes | Valid email format |
| password | Yes | Minimum 8 characters |
| fullName | Yes | 2-100 characters |
| mobile | Yes | Indian mobile: starts with 6-9, exactly 10 digits |
| role | No | `USER` (default) or `ADMIN` |

**Success Response** `201 Created`:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "userId": 1,
    "email": "john@example.com",
    "role": "USER"
  }
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Validation failed (invalid email, short password, invalid mobile) |
| 409 | Email already registered |

**Postman Test**:
```
POST {{baseUrl}}/api/auth/register
Body (raw JSON):
{
  "email": "testuser@omnicharge.com",
  "password": "Test@1234",
  "fullName": "Test User",
  "mobile": "9876543210"
}
Expected: 201 Created
```

---

### POST /api/auth/login

Login with email and password.

**Auth required**: No

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "Test@1234"
}
```

**Success Response** `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "userId": 1,
    "email": "john@example.com",
    "role": "USER"
  }
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Invalid email format or missing fields |
| 401 | Wrong password or user not found |

**Negative Test Cases**:
- Wrong password → expects `401`
- Invalid email format (e.g. `notanemail`) → expects `400`
- Non-existent email → expects `401`

**Postman Test**:
```
POST {{baseUrl}}/api/auth/login
Body:
{
  "email": "testuser@omnicharge.com",
  "password": "Test@1234"
}
Expected: 200 OK with accessToken
```

---

### POST /api/auth/refresh

Get a new access token using a refresh token.

**Auth required**: No

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Success Response** `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": { ... }
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Missing refreshToken |
| 401 | Expired or invalid refresh token |

---

### GET /api/auth/validate

Validate a JWT access token. Used internally by the API gateway.

**Auth required**: No (pass token in header manually)

**Request Header**:
```
Authorization: Bearer <access_token>
```

**Success Response** `200 OK`:
```json
{
  "valid": true,
  "email": "john@example.com",
  "userId": 1,
  "role": "USER"
}
```

---

### PUT /api/auth/password

Change the current user's password. Revokes all active sessions on success.

**Auth required**: Yes (Bearer token)

**Request Body**:
```json
{
  "currentPassword": "Test@1234",
  "newPassword": "NewTest@1234"
}
```

**Success Response** `204 No Content` (empty body)

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | New password too short (min 8 chars) |
| 401 | Current password is wrong |

---

### POST /api/auth/logout

Logout from current device — revokes the provided refresh token.

**Auth required**: Yes (Bearer token)

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Success Response** `204 No Content`

---

### POST /api/auth/logout-all

Logout from all devices — revokes all refresh tokens for the user.

**Auth required**: Yes (Bearer token)

**Request Body**: None

**Success Response** `204 No Content`

---

## User Service APIs

**Base path**: `/api/users`, `/api/admin/users`  
**Port**: 8081 (accessed via gateway at 8080)

---

### GET /api/users/me

Get the current logged-in user's profile.

**Auth required**: Yes

**Success Response** `200 OK`:
```json
{
  "id": 1,
  "email": "john@example.com",
  "mobile": "9876543210",
  "fullName": "John Doe",
  "role": "USER",
  "enabled": true
}
```

---

### PUT /api/users/me

Update the current user's profile.

**Auth required**: Yes

**Request Body**:
```json
{
  "fullName": "John Updated",
  "mobile": "9876543211"
}
```

**Field Rules**:
| Field | Required | Rules |
|---|---|---|
| fullName | No | 2-100 characters |
| mobile | No | Indian mobile format |

**Success Response** `200 OK`: Updated UserResponse

---

### GET /api/users/{id}

Get a user by their ID.

**Auth required**: Yes

**Path Parameter**: `id` — user's numeric ID

**Success Response** `200 OK`: UserResponse

**Error Responses**:
| Status | Reason |
|---|---|
| 404 | User not found |

---

### GET /api/admin/users

Get all registered users. Admin only.

**Auth required**: Yes (ADMIN role)

**Success Response** `200 OK`:
```json
[
  {
    "id": 1,
    "email": "john@example.com",
    "mobile": "9876543210",
    "fullName": "John Doe",
    "role": "USER",
    "enabled": true
  },
  ...
]
```

**Error Responses**:
| Status | Reason |
|---|---|
| 403 | Not an admin |

---

### GET /api/users/me/recharges

View current user's recharge history.

**Auth required**: Yes

**Success Response** `200 OK`:
```json
[
  {
    "id": 1,
    "rechargeId": "RCH-20240501-001",
    "mobileNumber": "9876543210",
    "operatorName": "Jio",
    "planName": "Unlimited 28 Days",
    "amount": 239.00,
    "validityDays": 28,
    "status": "COMPLETED",
    "transactionId": "TXN-001",
    "createdAt": "2024-05-01 10:00:00",
    "completedAt": "2024-05-01 10:00:05"
  }
]
```

---

### GET /api/users/me/recharges/{rechargeId}

Get a specific recharge by its recharge ID.

**Auth required**: Yes

**Path Parameter**: `rechargeId` — e.g. `RCH-20240501-001`

**Success Response** `200 OK`: RechargeResponse

---

### GET /api/users/me/transactions

View current user's payment transaction history.

**Auth required**: Yes

**Success Response** `200 OK`:
```json
[
  {
    "id": 1,
    "transactionId": "TXN-001",
    "rechargeId": "RCH-20240501-001",
    "userId": 1,
    "amount": 239.00,
    "description": "Recharge payment",
    "status": "SUCCESS",
    "paymentMethod": "WALLET",
    "createdAt": "2024-05-01 10:00:00",
    "processedAt": "2024-05-01 10:00:05"
  }
]
```

---

### GET /api/users/me/transactions/{transactionId}

Get status of a specific transaction.

**Auth required**: Yes

**Path Parameter**: `transactionId` — e.g. `TXN-001`

**Success Response** `200 OK`: TransactionResponse

---

### GET /api/users/me/recharges/{rechargeId}/transaction

Get the payment transaction linked to a specific recharge.

**Auth required**: Yes

**Path Parameter**: `rechargeId`

**Success Response** `200 OK`: TransactionResponse

---

## Operator Service APIs

**Base path**: `/api/operators`, `/api/plans`, `/api/admin/operators`, `/api/admin/plans`  
**Port**: 8084 (accessed via gateway at 8080)

> Public GET endpoints — no auth needed  
> Admin POST/PUT/DELETE — requires ADMIN role

---

### GET /api/operators

Get all active telecom operators.

**Auth required**: No

**Success Response** `200 OK`:
```json
[
  {
    "id": 1,
    "name": "Jio",
    "code": "JIO",
    "description": "Reliance Jio Infocomm",
    "active": true
  }
]
```

---

### GET /api/operators/{id}

Get a specific operator by ID.

**Auth required**: No

**Success Response** `200 OK`: OperatorResponse

**Error Responses**:
| Status | Reason |
|---|---|
| 404 | Operator not found |

---

### POST /api/admin/operators

Create a new telecom operator. Admin only.

**Auth required**: Yes (ADMIN role)

**Request Body**:
```json
{
  "name": "Jio",
  "code": "JIO",
  "description": "Reliance Jio Infocomm"
}
```

**Field Rules**:
| Field | Required | Rules |
|---|---|---|
| name | Yes | Max 50 characters |
| code | Yes | Max 10 characters |
| description | No | Free text |

**Success Response** `201 Created`: OperatorResponse

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Validation failed |
| 403 | Not an admin |

---

### PUT /api/admin/operators/{id}

Update an existing operator. Admin only.

**Auth required**: Yes (ADMIN role)

**Request Body**: Same as create

**Success Response** `200 OK`: Updated OperatorResponse

---

### DELETE /api/admin/operators/{id}

Deactivate an operator (soft delete). Admin only.

**Auth required**: Yes (ADMIN role)

**Success Response** `204 No Content`

---

### GET /api/operators/{operatorId}/plans

Get all active plans for a specific operator.

**Auth required**: No

**Success Response** `200 OK`: Array of PlanResponse

---

### GET /api/plans

Get all active recharge plans.

**Auth required**: No

**Success Response** `200 OK`:
```json
[
  {
    "id": 1,
    "name": "Unlimited 28 Days",
    "price": 239.00,
    "validityDays": 28,
    "data": "1.5GB/day",
    "calls": "Unlimited",
    "sms": "100/day",
    "description": "Popular prepaid plan",
    "type": "PREPAID",
    "active": true,
    "operatorId": 1,
    "operatorName": "Jio"
  }
]
```

---

### GET /api/plans/{planId}

Get a specific plan by ID.

**Auth required**: No

**Success Response** `200 OK`: PlanResponse

---

### POST /api/admin/plans

Create a new recharge plan. Admin only.

**Auth required**: Yes (ADMIN role)

**Request Body**:
```json
{
  "name": "Unlimited 28 Days",
  "price": 239.00,
  "validityDays": 28,
  "data": "1.5GB/day",
  "calls": "Unlimited",
  "sms": "100/day",
  "description": "Popular prepaid plan",
  "type": "PREPAID",
  "operatorId": 1
}
```

**Field Rules**:
| Field | Required | Rules |
|---|---|---|
| name | Yes | Max 100 characters |
| price | Yes | Minimum 1.0 |
| validityDays | Yes | Minimum 1 |
| type | Yes | e.g. `PREPAID`, `POSTPAID` |
| operatorId | Yes | Must be a valid operator ID |

**Success Response** `201 Created`: PlanResponse

---

### DELETE /api/admin/plans/{planId}

Deactivate a plan. Admin only.

**Auth required**: Yes (ADMIN role)

**Success Response** `204 No Content`

---

## Recharge Service APIs

**Base path**: `/api/recharges`  
**Port**: 8082 (accessed via gateway at 8080)

---

### POST /api/recharges

Initiate a mobile recharge.

**Auth required**: Yes

**Request Body**:
```json
{
  "mobileNumber": "9876543210",
  "operatorId": 1,
  "planId": 1
}
```

**Field Rules**:
| Field | Required | Rules |
|---|---|---|
| mobileNumber | Yes | Indian mobile: starts with 6-9, exactly 10 digits |
| operatorId | Yes | Valid operator ID |
| planId | Yes | Valid plan ID |

**Success Response** `201 Created`:
```json
{
  "id": 1,
  "rechargeId": "RCH-20240501-001",
  "mobileNumber": "9876543210",
  "operatorName": "Jio",
  "planName": "Unlimited 28 Days",
  "amount": 239.00,
  "validityDays": 28,
  "status": "COMPLETED",
  "transactionId": "TXN-001",
  "createdAt": "2024-05-01 10:00:00",
  "completedAt": "2024-05-01 10:00:05"
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 400 | Invalid mobile number format |
| 401 | Missing or invalid token |
| 404 | Operator or plan not found |

**Negative Test Cases**:
- Mobile starting with `1` (e.g. `1234567890`) → expects `400`
- Mobile with less than 10 digits → expects `400`
- Invalid operatorId → expects `404`

---

### GET /api/recharges/history

Get recharge history for the currently logged-in user.

**Auth required**: Yes

**Success Response** `200 OK`: Array of RechargeResponse

---

### GET /api/recharges/{rechargeId}

Get a specific recharge by its recharge ID string.

**Auth required**: Yes

**Path Parameter**: `rechargeId` — e.g. `RCH-20240501-001`

**Success Response** `200 OK`: RechargeResponse

**Error Responses**:
| Status | Reason |
|---|---|
| 404 | Recharge not found |

---

### GET /api/recharges/id/{id}

Get a recharge by its database numeric ID.

**Auth required**: Yes

**Path Parameter**: `id` — numeric DB id

**Success Response** `200 OK`: RechargeResponse

---

### GET /api/recharges/history/user/{userId}

Get recharge history for a specific user by their ID. Internal use.

**Auth required**: Yes

**Path Parameter**: `userId`

**Success Response** `200 OK`: Array of RechargeResponse

---

## Payment Service APIs

**Base path**: `/api/payments`  
**Port**: 8083 (accessed via gateway at 8080)

---

### GET /api/payments/transaction/{transactionId}

Get a payment transaction by its transaction ID.

**Auth required**: Yes

**Path Parameter**: `transactionId` — e.g. `TXN-001`

**Success Response** `200 OK`:
```json
{
  "id": 1,
  "transactionId": "TXN-001",
  "rechargeId": "RCH-20240501-001",
  "userId": 1,
  "amount": 239.00,
  "description": "Recharge payment",
  "status": "SUCCESS",
  "paymentMethod": "WALLET",
  "createdAt": "2024-05-01 10:00:00",
  "processedAt": "2024-05-01 10:00:05"
}
```

**Error Responses**:
| Status | Reason |
|---|---|
| 404 | Transaction not found |

---

### GET /api/payments/recharge/{rechargeId}

Get the payment transaction linked to a specific recharge.

**Auth required**: Yes

**Path Parameter**: `rechargeId`

**Success Response** `200 OK`: TransactionResponse

---

### GET /api/payments/user/{userId}

Get all payment transactions for a specific user.

**Auth required**: Yes

**Path Parameter**: `userId`

**Success Response** `200 OK`: Array of TransactionResponse

---

### GET /api/payments/admin/all

Get all payment transactions in the system. Admin only.

**Auth required**: Yes (ADMIN role)

**Success Response** `200 OK`: Array of TransactionResponse

**Error Responses**:
| Status | Reason |
|---|---|
| 403 | Not an admin |

**Negative Test Cases**:
- Regular user accessing this endpoint → expects `403`

---

## Health Check APIs

All services expose health endpoints via Spring Boot Actuator.

| Service | URL |
|---|---|
| API Gateway | `http://4.186.25.145:8080/actuator/health` |
| Auth Service | `http://4.186.25.145:8086/actuator/health` |
| User Service | `http://4.186.25.145:8081/actuator/health` |
| Recharge Service | `http://4.186.25.145:8082/actuator/health` |
| Payment Service | `http://4.186.25.145:8083/actuator/health` |
| Operator Service | `http://4.186.25.145:8084/actuator/health` |
| Notification Service | `http://4.186.25.145:8085/actuator/health` |

**Auth required**: No

**Success Response** `200 OK`:
```json
{
  "status": "UP"
}
```

---

## Error Reference

| HTTP Status | Meaning | Common Causes |
|---|---|---|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created successfully |
| 204 | No Content | Success with no response body (logout, delete) |
| 400 | Bad Request | Validation failed, missing required fields, invalid format |
| 401 | Unauthorized | Missing token, expired token, wrong password |
| 403 | Forbidden | Valid token but insufficient role (e.g. USER accessing ADMIN endpoint) |
| 404 | Not Found | Resource doesn't exist (user, operator, plan, recharge) |
| 409 | Conflict | Duplicate resource (e.g. email already registered) |
| 500 | Internal Server Error | Server-side bug or database issue |

---

## Testing Guide

### Step 1 — Import Collection
1. Open Postman
2. Click **Import**
3. Select `OmniCharge.postman_collection.json`
4. Collection appears with `baseUrl` = `http://4.186.25.145:8080`

### Step 2 — Run Full Test Suite in Order

Run requests **01 through 56 in sequence**. Each request auto-saves IDs and tokens for the next one.

**Recommended order:**

```
Phase 1 — Auth Setup
  01 Register User
  02 Register Admin
  03 Login User        ← saves accessToken, userId
  04 Login Admin       ← saves adminAccessToken

Phase 2 — Auth Verification
  05 Login Invalid Password  ← should get 401
  06 Login Invalid Email     ← should get 400
  07 Validate Token
  08 Refresh Token
  09 Change Password
  10 Login with New Password
  11 Logout All
  12 Login After Logout All
  13 Logout
  14 Login Final       ← fresh tokens for rest of tests

Phase 3 — User Profile
  15 Get My Profile
  16 Update My Profile
  17 Get User by ID
  18 Get All Users (Admin)
  19 Get All Users (Unauthorized) ← should get 403

Phase 4 — Operators & Plans
  22 Get All Operators (empty)
  23 Create Operator Unauthorized ← should get 403
  24 Create Operator Admin (Jio)  ← saves operatorId
  25 Create Operator 2 (Airtel)
  26 Get All Operators
  27 Get Operator by ID
  28 Update Operator
  29 Create Plan                  ← saves planId
  30 Create Plan 2
  31 Get All Plans
  32 Get Plan by ID
  33 Get Plans by Operator

Phase 5 — Recharge
  34 Initiate Recharge            ← saves rechargeId
  35 Initiate Recharge Invalid Mobile ← should get 400
  36 Get My Recharge History
  37 Get Recharge by Recharge ID
  38 Get Recharge by DB ID
  39 Get Recharge History by User ID

Phase 6 — Payments
  40 Get Transaction by Recharge ID ← saves transactionId
  41 Get Transaction by Transaction ID
  42 Get Transactions by User ID
  43 Get All Transactions (Admin)
  44 Get All Transactions (Unauthorized) ← should get 403

Phase 7 — Dashboard
  45-49 User dashboard endpoints

Phase 8 — Health Checks
  50-56 All service health checks
```

### Step 3 — Use Collection Runner for Automated Testing

1. Click the collection name → **Run collection**
2. Select all requests
3. Click **Run OmniCharge API**
4. View pass/fail results for all 56 requests

### Step 4 — Understanding Test Results

Each request has automated assertions. In the test results:
- ✅ **Green** = test passed (API behaved correctly)
- ❌ **Red** = test failed (API returned unexpected response)

**Negative tests** (05, 06, 19, 23, 35, 44) are expected to return error codes — they pass when the API correctly rejects bad input.

### Step 5 — Manual Testing Tips

**Test with Swagger UI:**
```
http://4.186.25.145:8080/swagger-ui.html
```
- Use the dropdown to switch between services
- Click **Authorize** → paste your accessToken
- Try endpoints directly in the browser

**Monitor services:**
```
http://4.186.25.145:8761        ← Eureka (all registered services)
http://4.186.25.145:15672       ← RabbitMQ (login: omnicharge / OmniRabbit@2024)
http://4.186.25.145:9411        ← Zipkin (distributed tracing)
```

---

## Service Port Reference

| Service | Internal Port | Access via Gateway |
|---|---|---|
| API Gateway | 8080 | Direct |
| Auth Service | 8086 | `8080/api/auth/**` |
| User Service | 8081 | `8080/api/users/**` |
| Recharge Service | 8082 | `8080/api/recharges/**` |
| Payment Service | 8083 | `8080/api/payments/**` |
| Operator Service | 8084 | `8080/api/operators/**`, `8080/api/plans/**` |
| Notification Service | 8085 | Event-driven (no REST endpoints) |
| Config Server | 8888 | Internal only |
| Service Discovery | 8761 | Dashboard only |
