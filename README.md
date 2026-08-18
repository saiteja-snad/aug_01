# QuickBite – Food Delivery Platform Backend

A complete Spring Boot backend for a food delivery platform, covering the full flow:

```
Customer → Restaurant → Cart → Order → Payment → Delivery → Notification
```

## Tech Stack

- **Java 17**, **Spring Boot 3.3**
- **Spring Security 6** with **JWT** (role-based access control)
- **Spring Data JPA** + **PostgreSQL**
- **springdoc-openapi** (Swagger UI)
- **SLF4J / Logback** (console + rolling file logging, plus a request-logging filter)
- **Docker** / **Docker Compose**
- **Lombok**

## Features

| Module | Description |
|---|---|
| Auth | Register/login, JWT issuance, roles: `CUSTOMER`, `RESTAURANT_OWNER`, `DELIVERY_AGENT`, `ADMIN` |
| Restaurants | CRUD for restaurant owners, public browse/search |
| Menu | Per-restaurant menu item CRUD |
| Cart | Add/update/remove items, restaurant-scoped cart |
| Orders | Place order from cart, view, history, status updates |
| Payments | Simulated payment gateway, tied 1:1 to an order |
| Delivery | Agent assignment, status updates, live tracking |
| Notifications | In-app notifications for order/payment/delivery events |

## Project Structure

```
src/main/java/com/quickbite/
  config/        # Security, Swagger, request-logging filter config
  security/      # JWT util, filter, UserDetails
  entity/        # JPA entities + enums
  repository/    # Spring Data repositories
  dto/           # request/ and response/ DTOs
  service/       # interfaces + impl/
  controller/    # REST controllers
  exception/     # Global exception handling
```

## Running Locally with Docker (recommended)

```bash
docker compose up --build
```

This starts PostgreSQL and the app. The API will be available at:
- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Running Locally without Docker

1. Start a PostgreSQL instance and create a database `quickbite_db`.
2. Set environment variables (or edit `application.yml` defaults):
   ```
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=quickbite_db
   DB_USER=quickbite
   DB_PASSWORD=quickbite123
   JWT_SECRET=<a-long-random-secret>
   ```
3. Run:
   ```bash
   mvn spring-boot:run
   ```

## Authentication Flow

1. `POST /api/auth/register` — create a user with a `role` of `CUSTOMER`, `RESTAURANT_OWNER`, `DELIVERY_AGENT`, or `ADMIN`.
2. `POST /api/auth/login` — returns a JWT.
3. Send the token on every subsequent request:
   ```
   Authorization: Bearer <token>
   ```

## Key API Endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Register |
| POST | `/api/auth/login` | public | Login |
| POST | `/api/restaurants` | RESTAURANT_OWNER | Create restaurant |
| GET | `/api/restaurants` | public | List restaurants |
| GET | `/api/restaurants/search?keyword=` | public | Search |
| POST | `/api/restaurants/{id}/menu-items` | RESTAURANT_OWNER | Add menu item |
| GET | `/api/restaurants/{id}/menu-items` | public | View menu |
| GET/POST | `/api/cart` | CUSTOMER | View / add to cart |
| POST | `/api/orders` | CUSTOMER | Place order from cart |
| GET | `/api/orders/history` | CUSTOMER | Order history |
| PATCH | `/api/orders/{id}/status` | RESTAURANT_OWNER, ADMIN | Update order status |
| POST | `/api/payments` | CUSTOMER | Pay for an order |
| POST | `/api/deliveries/order/{id}/assign` | RESTAURANT_OWNER, ADMIN | Assign delivery agent |
| PATCH | `/api/deliveries/order/{id}/status` | DELIVERY_AGENT | Update delivery status |
| GET | `/api/deliveries/order/{id}/track` | authenticated | Track delivery |
| GET | `/api/notifications` | authenticated | List notifications |

Full interactive documentation is available via Swagger UI once the app is running.

## Logging

- Console output for local dev.
- Rolling file logs under `logs/quickbite.log` (daily rotation, 30-day history, 10MB max file size).
- Every HTTP request is logged with method, path, status, and duration via `RequestLoggingFilter`.
- Service-layer actions (order placed, payment processed, delivery assigned, etc.) are logged at INFO level; validation/auth failures at WARN; unhandled errors at ERROR.

## Notes on Design Choices

- **Cart is restaurant-scoped**: adding an item from a different restaurant clears the existing cart, matching typical food-delivery UX.
- **Payment is a simulated gateway**: it auto-approves and generates a transaction ID; swap `PaymentServiceImpl` for a real provider integration (Stripe, Razorpay, etc.) in production.
- **Soft-delete for restaurants**: `DELETE /api/restaurants/{id}` deactivates rather than hard-deletes, to preserve order history integrity.
- **`ddl-auto: update`** is used for convenience; switch to Flyway/Liquibase migrations for production.
