# Copilot Instructions for Restaurant Project

## Project Overview
This is a full-stack monorepo application containing:
- **Server**: `restaurant-server` (Spring Boot 4, Kotlin, WebFlux, R2DBC)
- **Web**: `restaurant-web` (Angular 20, Standalone Components)
- **Android**: `restaurant-android` (Native Android, Kotlin, ViewBinding)

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
  - `security/`: JWT and auth configuration.

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

## Web (`restaurant-web`)

### Architecture & Stack
- **Framework**: Angular 20.3.
- **UI Library**: Angular Material 20.2.
- **Structure**: Feature-based in `src/app/`:
  - `core/`: Singleton services, models, guards, interceptors.
  - `features/`: Feature modules (e.g., `auth`, `cart-page`, `restaurant-menu`, `vip-page`).

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

## Android (`restaurant-android`)

### Architecture & Stack
- **Language**: Kotlin 2.0.21.
- **SDK**: Min 24, Target 36.
- **Package**: `com.yaojia.snowball`
- **UI System**: View-based (XML) with **ViewBinding**.
- **Navigation**: Jetpack Navigation Component (Single Activity `MainActivity`, Multiple Fragments).
- **Architecture**: MVVM (Model-View-ViewModel).

### Structure
- `ui/`: Feature-based packages containing Fragments and ViewModels.
  - `ui/tables/`: `TablesFragment`, `TablesViewModel`.
  - `ui/kitchen/`: Kitchen view features.
  - `ui/staff/`: Staff management features.
- `data/`: Repositories and data models.

### Coding Conventions
- **UI Access**:
  - **ALWAYS** use `ViewBinding`. Do NOT use `findViewById` or Kotlin Synthetics.
  - Example: `binding.textView.text = "Hello"`
- **Navigation**:
  - Use `findNavController().navigate()` for transitions.
  - Define arguments in `navigation/mobile_navigation.xml` (Safe Args).
- **State**:
  - Use `ViewModel` to hold UI state.
  - Expose state via `LiveData` or `StateFlow` (prefer `StateFlow` for new code).
- **Threading**:
  - Use `viewModelScope` for coroutines launched from ViewModels.

## Workflows

### Development
1. **Database**: `docker-compose up -d` (MySQL).
2. **Server**: `./gradlew bootRun` (in `restaurant-server`).
3. **Web**: `ng serve` (in `restaurant-web`).
4. **Android**: Open in Android Studio or run `./gradlew installDebug` (in `restaurant-android`).

### Testing
- **Server**: `./gradlew test`
- **Web**: `ng test`
- **Android**: `./gradlew test` (Unit), `./gradlew connectedAndroidTest` (Instrumentation).
