# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

창고형 식자재 마트 (Warehouse-style Food Mart) - 재고관리 + 주문 시스템. Spring Boot 4.0.2 / Java 17 / MySQL / Gradle project.

## Build & Test Commands

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.food_mart.process_test.Order.StockServiceTest"

# Run a single test method
./gradlew test --tests "com.example.food_mart.process_test.Order.StockServiceTest.getItemStockList_test"

# Run the application
./gradlew bootRun
```

Database: MySQL on `localhost:3306/mart`, user `spring.mart`. JPA ddl-auto is `update`.

## Architecture

Module-based package structure under `com.example.food_mart.modules`:

- **order** - 주문 처리. `OrderService` orchestrates the full order flow: cart setup -> buyability check -> order creation -> payment -> cart clear. `TransactionService` handles order+payment persistence. `LedgerPaymentService` implements `PaymentService` interface.
- **shop** - 상품/장바구니. `Item`, `Category`, `Cart` (domain object, not entity), `ItemInCart` (entity). `CartService` manages cart operations and stock availability checks.
- **warehouse** - 창고/재고. `Stock`, `Warehouse`, `Picking`. `StockService` handles stock queries and pessimistic-lock stock deduction (`stockToOutPrepare`). `StackingService` for inbound stacking.
- **logistic** - 물류. `Inbound`/`InboundItem`, `Outbound`, `Delivery`. Manages inbound receiving and outbound shipping.
- **user** - 사용자/지갑. `User`, `Wallet`, `UserSignService`. Session-based auth (no Spring Security yet).
- **staff** - 직원. `Staff`, `StaffRole`, `StaffService`.

### Cross-cutting (`common/`)

- **`ApiResponse<T>`** - Unified response wrapper. Use `ApiResponse.success(data)` in controllers, `ApiResponse.error()` in exception handler.
- **`ErrorCode` enum** - Centralized error codes with HttpStatus and Korean message. Add new errors here.
- **`GlobalExceptionHandler`** - Catches `Expected4xxException` and `Expected5xxException`.
- **`UserInfo` / `StaffInfo`** argument resolvers - Inject current user/staff from HTTP session into controller methods. Registered in `WebMvcConfig`.

### Key Patterns

- Stock deduction uses pessimistic locking via `StockRepository.findByIdWithPessimisticLock`.
- `Cart` is a plain domain object (not a JPA entity) assembled at order time from `ItemInCart` entities.

## API Documentation
Swagger UI available at `/swagger-ui/index.html` when the app is running (springdoc-openapi).
