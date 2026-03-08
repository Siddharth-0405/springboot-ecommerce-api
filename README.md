<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:4f46e5,100:06b6d4&height=200&section=header&text=Product%20Service%20API&fontSize=52&fontColor=ffffff&fontAlignY=38&desc=Production-Ready%20Spring%20Boot%20Microservice&descAlignY=60&descSize=18" width="100%"/>

<br/>

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui/index.html)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

<br/>

> **A fully production-grade REST API** for an e-commerce platform, built with Spring Boot 3, JWT authentication, Redis caching, and PostgreSQL — containerized with Docker Compose and documented with Swagger UI.

<br/>

[**View API Docs →**](http://localhost:8080/swagger-ui/index.html) &nbsp;·&nbsp; [**Report Bug**](issues) &nbsp;·&nbsp; [**Request Feature**](issues)

<br/>

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [API Reference](#-api-reference)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [Default Credentials](#-default-credentials)
- [Docker Setup](#-docker-setup)
- [Security](#-security)

---

## 🧭 Overview

**Product Service** is a fully featured, production-ready e-commerce backend microservice. It handles everything from product catalog management and shopping cart operations to order processing, user authentication, and product reviews — all secured with stateless JWT-based authentication and optimized with Redis caching.

Built to demonstrate real-world Spring Boot patterns: layered architecture, DTO mapping, global exception handling, role-based access control, async email notifications, and containerized deployment.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT / SWAGGER UI                       │
│                     http://localhost:8080                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP/REST
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                       │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐ │
│  │  Controllers │→ │   Services   │→ │     Repositories       │ │
│  │  Auth        │  │  Auth        │  │  UserRepository        │ │
│  │  Products    │  │  Product     │  │  ProductRepository     │ │
│  │  Categories  │  │  Category    │  │  CategoryRepository    │ │
│  │  Cart        │  │  Cart        │  │  CartRepository        │ │
│  │  Orders      │  │  Order       │  │  OrderRepository       │ │
│  │  Reviews     │  │  Review      │  │  ReviewRepository      │ │
│  │  Admin       │  │  AdminStats  │  └───────────┬────────────┘ │
│  └──────────────┘  │  Email       │              │              │
│                    └──────────────┘              │ JPA/Hibernate│
│  ┌──────────────────────────────┐                │              │
│  │     Security Layer (JWT)     │                ▼              │
│  │  JwtAuthenticationFilter     │  ┌─────────────────────────┐  │
│  │  JwtTokenProvider            │  │     PostgreSQL 16        │  │
│  │  UserDetailsServiceImpl      │  │  productdb               │  │
│  └──────────────────────────────┘  └─────────────────────────┘  │
│                                                                   │
│  ┌──────────────────────────────┐                               │
│  │       Redis Cache            │  Cache TTL: 10 minutes        │
│  │   Product listings           │  Max Memory: 256mb            │
│  │   Category data              │  Policy: allkeys-lru          │
│  └──────────────────────────────┘                               │
└─────────────────────────────────────────────────────────────────┘

           Docker Compose Network: backend-net (bridge)
    ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
    │   postgres   │  │    redis     │  │  product-service │
    │  :5432       │  │  :6379       │  │  :8080           │
    └──────────────┘  └──────────────┘  └──────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Framework | Spring Boot | 3.3.0 |
| Language | Java | 17 |
| Database | PostgreSQL | 16 (Alpine) |
| Cache | Redis | 7 (Alpine) |
| ORM | Spring Data JPA / Hibernate | — |
| Security | Spring Security + JWT (jjwt) | 0.12.5 |
| API Docs | SpringDoc OpenAPI / Swagger UI | 2.5.0 |
| Containerization | Docker + Docker Compose | — |
| Build Tool | Maven | 3.9.6 |
| Boilerplate | Lombok | — |
| Validation | Jakarta Bean Validation | — |
| Email | Spring Mail (SMTP) | — |
| Monitoring | Spring Actuator | — |
| Runtime | Eclipse Temurin JRE (Alpine) | 17 |

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based stateless authentication with configurable expiration (default 24h)
- Role-based access control: `ROLE_USER` and `ROLE_ADMIN`
- Secure password hashing with BCrypt
- Method-level security with `@PreAuthorize`

### 📦 Product Management
- Full CRUD with pagination and sorting
- Search by name and price range
- Low-stock alerts (threshold: 10 units)
- SKU-based product identification
- Admin stock management (add / reduce)

### 🛒 Shopping Cart
- Persistent cart per user
- Add, update quantity, remove items
- Clear entire cart

### 📋 Order Management
- Place orders from cart
- Order lifecycle: `PENDING → CONFIRMED → SHIPPED → DELIVERED → CANCELLED`
- User order history with pagination
- Admin order status management
- Order cancellation by user

### ⭐ Product Reviews
- One review per user per product
- Star rating with text content
- Update and delete own reviews

### 🧑‍💼 Admin Dashboard
- Overview stats (total products, users, orders, revenue)
- All orders management with status updates
- Low-stock product monitoring
- Stock level management

### ⚡ Performance
- Redis caching on product and category queries (10-minute TTL)
- HikariCP connection pool (min 5, max 20 connections)
- Async email notifications (non-blocking)
- Multi-stage Docker build (builder + minimal runtime image)
- G1GC garbage collector with container-aware memory settings

### 🛡️ Production Hardening
- Global exception handler with structured error responses
- Input validation on all DTOs
- Non-root Docker user (`appuser`)
- Health checks on all three containers
- Persistent volumes for PostgreSQL and Redis data
- Actuator endpoints: `/actuator/health`, `/actuator/info`, `/actuator/metrics`

---

## 📁 Project Structure

```
product-service-v2/
├── src/
│   └── main/
│       ├── java/com/productservice/
│       │   ├── config/
│       │   │   ├── AsyncConfig.java          # @EnableAsync thread pool
│       │   │   ├── DataSeeder.java           # Seeds admin user + sample data
│       │   │   ├── OpenApiConfig.java        # Swagger/OpenAPI configuration
│       │   │   ├── RedisConfig.java          # Redis serialization config
│       │   │   └── SecurityConfig.java       # JWT filter chain + RBAC rules
│       │   ├── controller/
│       │   │   ├── AdminController.java      # /api/v1/admin/**
│       │   │   ├── AuthController.java       # /api/v1/auth/**
│       │   │   ├── CartController.java       # /api/v1/cart/**
│       │   │   ├── CategoryController.java   # /api/v1/categories/**
│       │   │   ├── OrderController.java      # /api/v1/orders/**
│       │   │   ├── ProductController.java    # /api/v1/products/**
│       │   │   └── ReviewController.java     # /api/v1/products/{id}/reviews/**
│       │   ├── dto/                          # Request/Response DTOs (14 classes)
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── ResourceNotFoundException.java
│       │   │   ├── DuplicateResourceException.java
│       │   │   └── ErrorResponse.java
│       │   ├── model/
│       │   │   ├── User.java                 # Implements UserDetails
│       │   │   ├── Product.java
│       │   │   ├── Category.java
│       │   │   ├── Cart.java + CartItem.java
│       │   │   ├── Order.java + OrderItem.java
│       │   │   ├── Review.java
│       │   │   ├── Role.java                 # Enum: ROLE_USER, ROLE_ADMIN
│       │   │   └── OrderStatus.java          # Enum: PENDING→DELIVERED
│       │   ├── repository/                   # 6 JPA Repositories
│       │   ├── security/
│       │   │   ├── JwtTokenProvider.java     # Token generation & validation
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── service/                      # Service interfaces + implementations
│       └── resources/
│           └── application.yml               # Full app configuration
├── Dockerfile                                # Multi-stage build
├── docker-compose.yml                        # 3-service orchestration
└── pom.xml
```

---

## 📡 API Reference

All endpoints are prefixed with `/api/v1`. Full interactive docs at [`/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).

### 🔑 Authentication — `/api/v1/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/auth/register` | Public | Register a new user account |
| `POST` | `/auth/login` | Public | Login and receive JWT token |

<details>
<summary><b>POST /auth/login — Example</b></summary>

**Request:**
```json
{
  "email": "admin@store.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@store.com",
    "role": "ROLE_ADMIN"
  }
}
```
</details>

---

### 📦 Products — `/api/v1/products`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/products` | Public | List all products (paginated) |
| `GET` | `/products/{id}` | Public | Get product by ID |
| `GET` | `/products/search` | Public | Search by name and price range |
| `POST` | `/products` | Admin | Create a new product |
| `PUT` | `/products/{id}` | Admin | Update a product |
| `DELETE` | `/products/{id}` | Admin | Delete a product |

**Query params for `GET /products`:** `page`, `size`, `sortBy`, `sortDir`

**Query params for `GET /products/search`:** `name`, `minPrice`, `maxPrice`, `page`, `size`

---

### 🗂️ Categories — `/api/v1/categories`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/categories` | Public | List all categories |
| `GET` | `/categories/{id}` | Public | Get category by ID |
| `POST` | `/categories` | Admin | Create category |
| `PUT` | `/categories/{id}` | Admin | Update category |
| `DELETE` | `/categories/{id}` | Admin | Delete category |

---

### 🛒 Cart — `/api/v1/cart`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/cart` | User | Get current cart |
| `POST` | `/cart/items` | User | Add item to cart |
| `PUT` | `/cart/items/{cartItemId}` | User | Update item quantity |
| `DELETE` | `/cart/items/{cartItemId}` | User | Remove item from cart |
| `DELETE` | `/cart` | User | Clear entire cart |

---

### 📋 Orders — `/api/v1/orders`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/orders` | User | Place order from cart |
| `GET` | `/orders` | User | Get own order history |
| `GET` | `/orders/{id}` | User | Get specific order |
| `PUT` | `/orders/{id}/cancel` | User | Cancel an order |

---

### ⭐ Reviews — `/api/v1/products/{productId}/reviews`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/products/{id}/reviews` | User | Add a review |
| `GET` | `/products/{id}/reviews` | Public | Get all reviews for a product |
| `PUT` | `/products/{id}/reviews` | User | Update your review |
| `DELETE` | `/products/{id}/reviews` | User | Delete your review |

---

### 🧑‍💼 Admin — `/api/v1/admin`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/admin/stats` | Admin | Dashboard overview stats |
| `GET` | `/admin/orders` | Admin | List all orders |
| `PUT` | `/admin/orders/{id}/status` | Admin | Update order status |
| `GET` | `/admin/products/low-stock` | Admin | Products with < 10 units |
| `PUT` | `/admin/products/{id}/stock/add` | Admin | Add stock to product |
| `PUT` | `/admin/products/{id}/stock/reduce` | Admin | Reduce stock |

---

## 🚀 Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- Ports `8080`, `5432`, and `6379` available on your machine

### One-Command Start

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/product-service.git
cd product-service

# 2. Start all services
docker-compose up --build

# 3. Access the API
open http://localhost:8080/swagger-ui/index.html
```

That's it. The app auto-seeds an admin user and sample products on first run.

### Stopping the Service

```bash
# Stop and remove containers (always use this — never just close the terminal)
docker-compose down
```

---

## 🔧 Environment Variables

All variables have safe defaults for local development. Override via a `.env` file in the project root.

```env
# Database
DB_NAME=productdb
DB_USER=postgres
DB_PASSWORD=postgres

# Redis
REDIS_PASSWORD=

# JWT (change this in production!)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000

# Email (optional — for order confirmation emails)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

---

## 🔑 Default Credentials

The `DataSeeder` automatically creates these on first run:

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@store.com` | `admin123` |
| **User** | `user@store.com` | `user123` |

> ⚠️ Change these credentials before any public deployment.

---

## 🐳 Docker Setup

The `docker-compose.yml` orchestrates three containers on a shared `backend-net` bridge network:

```
product-service  →  depends_on (healthy)  →  postgres
                                           →  redis
```

**Container health checks:**
- `postgres` — `pg_isready` every 10s
- `redis` — `redis-cli ping` every 10s
- `product-service` — `actuator/health` every 30s

**Persistent volumes:**
- `postgres_data` — database files survive container restarts
- `redis_data` — Redis AOF persistence

**Multi-stage Dockerfile:**
- Stage 1 (`builder`): Maven 3.9.6 on Eclipse Temurin 17 Alpine — compiles and packages the JAR
- Stage 2 (`runtime`): Minimal JRE Alpine image — runs as non-root `appuser`
- Result: lean, secure production image

**JVM tuning (production-ready):**
```
-Xms256m -Xmx512m
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
-XX:+UseG1GC
```

---

## 🔒 Security

```
Request
  │
  ▼
JwtAuthenticationFilter
  │  ├─ Extract Bearer token from Authorization header
  │  ├─ Validate signature + expiry via JwtTokenProvider
  │  └─ Set SecurityContext if valid
  ▼
SecurityFilterChain
  │  ├─ Public:  /api/v1/auth/**
  │  ├─ Public:  GET /api/v1/products/**
  │  ├─ Public:  GET /api/v1/categories/**
  │  ├─ Public:  /swagger-ui/** · /api-docs/**
  │  ├─ Admin:   /api/v1/admin/**
  │  └─ User:    everything else (authenticated)
  ▼
Controller → @PreAuthorize("hasRole('ADMIN')")
```

JWT tokens are signed with HMAC-SHA256, expire in 24 hours, and are validated on every request — no server-side session state.

---

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:06b6d4,100:4f46e5&height=120&section=footer" width="100%"/>

**Built with ❤️ using Spring Boot 3 · PostgreSQL · Redis · Docker**

</div>
