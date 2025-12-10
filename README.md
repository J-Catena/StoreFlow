StoreFlow – Inventory & Sales Management System

StoreFlow is a backend system designed to model the real workflows of a small retail store:
product intake, inventory management, sales processing, and business-rule validation.
It is built with Spring Boot, follows domain-driven principles, and includes full service-level test coverage for all core features.

 Features
Inventory Management

Register products by serial number.

Automatically create products on first stock entry.

Maintain stock at STORE location.

Retrieve current stock instantly.

Prevent inconsistent states through validation.

Sales (Ticket System)

Create sales tickets (OPEN → CLOSED).

Add product lines to a ticket.

Auto-merge lines for repeated products.

Validate stock before closing a sale.

Deduct inventory only on ticket close.

Prevent:

Closing when stock is insufficient.

Modifying closed tickets.

Closing the same ticket twice.

Business Error Handling

Structured JSON error responses.

409 Conflict for business rule violations.

400 Bad Request for invalid input.

Test Coverage

Includes complete tests for:

InventoryService

SaleService

Stock operations

Business rule validation

Transactional consistency

All tests run under an in-memory H2 database for isolation and speed.

 Tech Stack

Java 21

Spring Boot

Spring Data JPA / Hibernate

H2 (Test)

JUnit 5 + AssertJ

Lombok

RESTful API

Global Exception Handling

 Folder Structure (Simplified)
src/
 └── main/java/com/jcatena/storeflow
     ├── inventory/     # stock entries, product creation, inventory queries
     ├── product/       # product entity + repository
     ├── sale/          # sale tickets, lines, closing logic
     ├── common/        # global exception handler + ApiError
     └── StoreFlowApplication.java

 └── test/java/com/jcatena/storeflow
     ├── inventory/InventoryServiceTest.java
     └── sale/SaleServiceTest.java

 API Endpoints
Inventory
Add stock
POST /api/inventory/add


Request:

{
  "serialNumber": "ABC123",
  "name": "Cuchilla 40cm",
  "quantity": 10
}

Get stock by serial number
GET /api/inventory/{serialNumber}


Response:

10

Sales
Create ticket
POST /api/sales/tickets


Request:

{
  "customerName": "Cliente Test"
}

Add line
POST /api/sales/tickets/{ticketId}/lines


Request:

{
  "serialNumber": "ABC123",
  "quantity": 3
}

Close ticket
POST /api/sales/tickets/{ticketId}/close


Successful close updates inventory and returns ticket response.
Failure produces:

{
  "error": "Conflict",
  "status": 409,
  "message": "Not enough stock for product ABC123. Requested 12, available 5",
  "path": "/api/sales/tickets/1/close",
  "timestamp": "2025-12-05T..."
}

 Running Tests
mvn test


Everything is transactional and isolated.
All critical domain rules are validated through unit and integration tests.

 Running the Application
With Maven:
mvn spring-boot:run


The app runs on:

http://localhost:8080

Profiles

The project includes a dev profile with a DevelopmentRunner for internal testing, disabled by default.

 Project Status

The core domain (inventory + sales) is fully implemented and tested.
Next planned modules:

Workshop module (repair orders + stock consumption)

Security using JWT

PostgreSQL integration + Docker Compose

API documentation (OpenAPI/Swagger)

 Why This Project Matters

This is not a CRUD.
It models real business rules found in stores and repair shops:

Stock doesn’t decrease until a ticket is closed.

Tickets can't be modified after closing.

Operations fail with clear business-driven errors.

Multiple modules share consistent domain behavior.

This demonstrates professional backend thinking:
validation, transactional logic, and state correctness.
