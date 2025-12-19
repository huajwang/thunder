# Copilot Instructions for Restaurant Project

## Project Overview
This is a full-stack monorepo application:
- **Server**: `restaurant-server` (Spring Boot 4, Kotlin, WebFlux, R2DBC)
- **Web**: `restaurant-web` (Angular 20, Standalone Components)

## Server (`restaurant-server`)

### Architecture & Stack
- **Framework**: Spring Boot 4.0.0 (Reactive).
- **Language**: Kotlin 2.2 (Java 24).
- **Database**: MySQL 8.4 via R2DBC.
- **Structure**: Layered architecture in `com.yaojia.restaurant_server`:
  - `controller/`: REST endpoints (`@RestController`).
  - `service/`: Business logic.
  - `repo/`: Data access (`CoroutineCrudRepository`).
  - `data/`: Entities (`data class`).
  - `dto/`: Data Transfer Objects.

### Coding Conventions
- **Reactive/Async**:
  - Use **Coroutines** (`suspend fun`) for single values.
  - Use **Flow** (`Flow<T>`) for streams/collections.
  - **Avoid** `Mono`/`Flux` types in method signatures unless absolutely necessary.
- **Controllers**:
  - Endpoints should be `suspend fun` or return `Flow<T>`.
  - Use `ResponseStatusException` for error handling.
  - Example: `suspend fun get(@PathVariable id: Long): Entity`
- **Dependency Injection**:
  - Prefer **constructor injection**.

### Testing
- Use `WebTestClient` for integration tests of controllers.
- Use `runTest` from `kotlinx-coroutines-test` for unit testing suspending functions.

### Build & Run
- **Run**: `./gradlew bootRun`
- **Test**: `./gradlew test`

## Web (`restaurant-web`)

### Architecture & Stack
- **Framework**: Angular 20.3.
- **UI Library**: Angular Material 20.2.
- **Structure**: Feature-based:
  - `src/app/core/`: Singleton services, models, guards, interceptors.
  - `src/app/features/`: Feature modules (e.g., `auth`, `restaurant-menu`).

### Coding Conventions
- **Components**:
  - Use **Standalone Components** (`standalone: true`).
  - Imports must be explicit in `@Component`.
- **State Management**:
  - **PREFER** Angular Signals (`signal()`, `computed()`, `effect()`) for local state.
  - Use RxJS for HTTP requests and complex event streams.
- **Dependency Injection**:
  - **ALWAYS** use `inject()` function instead of constructor injection.
  - Example: `private http = inject(HttpClient);`
- **Control Flow**:
  - Use `@if`, `@for`, `@switch` syntax.

### Integration
- **API**: Base URL is `http://localhost:8080/api`.
- **Real-time**: Uses Server-Sent Events (SSE) via `EventSource`.
- **Auth**: JWT stored in `localStorage` (`auth_token`).

## Workflows
- **Development**:
  1. Start DB: `docker-compose up -d` (MySQL).
  2. Start Server: `./gradlew bootRun` (in `restaurant-server`).
  3. Start Web: `ng serve` (in `restaurant-web`).
