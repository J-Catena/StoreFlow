StoreFlow
Inventory, Sales & Workshop Management Backend (Java / Spring Boot)

StoreFlow is a backend system designed to model real-world store and workshop workflows, not just CRUD operations.

It simulates how a small retail business actually operates:
products enter the store, stock is managed across locations, sales are processed through tickets, and a workshop consumes parts only after internal stock transfers.

The project focuses on business rules, state consistency, and transactional integrity, with full service-level test coverage.

 Core Concepts Modeled

StoreFlow is built around real business constraints:

Stock does not decrease until a sale or repair is closed

A workshop cannot consume parts directly from store stock

Internal stock transfers are required (STORE → WORKSHOP)

Closed operations cannot be modified

All critical actions are validated and fail fast with meaningful errors

This is a domain-driven backend, not a demo API.

 Functional Modules
 Inventory Management

Register products by serial number

Auto-create products on first stock entry

Maintain stock by location:

STORE

WORKSHOP

Transfer stock internally between locations

Prevent negative or inconsistent stock states

 Sales (Ticket System)

Create sales tickets (OPEN → CLOSED)

Add product lines (auto-merge repeated products)

Validate stock before closing

Deduct inventory only on ticket close

Prevent:

Closing with insufficient stock

Modifying closed tickets

Closing the same ticket twice

 Workshop (Repair Orders)

Create repair orders (OPEN → CLOSED)

Register parts used during a repair

Consume stock only from WORKSHOP

Enforce internal transfer rules

Prevent closing repairs without sufficient workshop stock

 Business Error Handling

The API returns clear, business-driven errors, not generic failures:

400 Bad Request → invalid input

409 Conflict → business rule violation

Example:

{
"error": "Conflict",
"status": 409,
"message": "Not enough workshop stock for product PZ123",
"path": "/api/workshop/orders/1/close",
"timestamp": "2025-12-05T..."
}

 Test Coverage

All core logic is protected by service-level tests:

InventoryService

SaleService

WorkshopService

Stock transfers

Business rule validation

Transactional consistency

Tests run using an in-memory H2 database, ensuring isolation and speed.

🛠 Tech Stack

Java 21

Spring Boot

Spring Data JPA / Hibernate

H2 (tests)

JUnit 5 + AssertJ

Lombok

RESTful API

Global Exception Handling

Transactional Services

 Project Structure (Simplified)
src/
├── main/java/com/jcatena/storeflow
│   ├── inventory/    # stock, locations, transfers
│   ├── product/      # product model
│   ├── sale/         # sales tickets & lines
│   ├── workshop/     # repair orders & consumption
│   ├── common/       # error handling
│   └── StoreFlowApplication.java
└── test/java/com/jcatena/storeflow
├── inventory/
├── sale/
└── workshop/

▶ Running the Application
mvn spring-boot:run


App runs on:

http://localhost:8080

▶ Running Tests
mvn test


All operations are transactional and fully isolated.

 Profiles

A dev profile includes a DevelopmentRunner used for internal domain testing.
Disabled by default.

 Project Status & Roadmap
 Implemented

Inventory (multi-location)

Sales

Workshop

Internal stock transfers

Full service-level test coverage

 Planned

REST controllers for workshop module

JWT-based security

PostgreSQL + Docker Compose

OpenAPI / Swagger documentation

 Why This Project Matters

This project demonstrates backend thinking beyond CRUD:

Business rules enforced at service level

State transitions explicitly modeled

Stock integrity guaranteed by transactions

Failures are meaningful and intentional

Modules interact consistently

It reflects the kind of backend logic found in real retail and repair environments, not tutorials.