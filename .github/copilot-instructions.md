# Copilot Instructions for Restaurant Project

## Project Overview
This workspace contains a full-stack application split into two main projects:
- **Server**: `restaurant-server` (Spring Boot 4, Kotlin, WebFlux)
- **Web**: `restaurant-web` (Angular 20)

## Server (`restaurant-server`)

### Architecture & Stack
- **Framework**: Spring Boot 4.0.0 (Reactive stack).
- **Language**: Kotlin 2.2 running on Java 24.
- **Database**: MySQL accessed via R2DBC (Reactive).
- **Concurrency**: Kotlin Coroutines & Flow.

### Coding Conventions
- **Async/Reactive**:
  - **ALWAYS** prefer Kotlin Coroutines (`suspend` functions) over raw `Mono`/`Flux` types for single values.
  - Use `Flow<T>` for streams of data instead of `Flux<T>`.
  - Controller endpoints should be `suspend fun`.
- **Data Access**:
  - Use `CoroutineCrudRepository` or `R2dbcRepository`.
  - Entities should be Kotlin `data class`es.
- **Dependency Injection**:
  - Prefer constructor injection.

### Testing
- Use `WebTestClient` for testing reactive endpoints.
- Use `@SpringBootTest` for integration tests.
- Ensure tests handle coroutine execution (use `runTest` from `kotlinx-coroutines-test` if needed).

### Build & Run
- **Run**: `./gradlew bootRun` (in `restaurant-server/`)
- **Test**: `./gradlew test`

## Web (`restaurant-web`)

### Architecture & Stack
- **Framework**: Angular 20.3.
- **Style**: Standalone Components (No NgModules).

### Coding Conventions
- **Components**:
  - Use **Standalone Components** exclusively.
  - Imports must be explicit in the `@Component` decorator.
- **State Management**:
  - **PREFER** Angular Signals (`signal()`, `computed()`, `effect()`) over RxJS `BehaviorSubject` for component state.
  - Use RxJS mainly for complex event streams or HTTP handling where Signals aren't sufficient yet.
- **Dependency Injection**:
  - **PREFER** the `inject()` function over constructor injection for cleaner code.
  - Example: `private route = inject(ActivatedRoute);`
- **Control Flow**:
  - Use the new built-in control flow syntax (`@if`, `@for`, `@switch`) instead of `*ngIf` and `*ngFor`.

### Build & Run
- **Run**: `ng serve` (in `restaurant-web/`)
- **Test**: `ng test`
- **Build**: `ng build`

## Common Workflows
- **New Feature**:
  1. Define the data model in `restaurant-server`.
  2. Create a `suspend` controller endpoint.
  3. Create an Angular service using `HttpClient`.
  4. Build the UI component using Signals.
