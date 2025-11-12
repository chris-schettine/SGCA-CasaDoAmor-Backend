# SGCA Casa do Amor Backend - AI Implementation Guide

## Project snapshot
- Java 21 with Spring Boot 3.3.5; build and dependency management handled by the Maven wrapper (`mvnw`, `mvnw.cmd`).
- Primary data store is MySQL 8 in production; integration and unit tests rely on the in-memory H2 profile defined in `src/main/resources/application-test.properties`.
- Source is organised under `src/main/java/br/com/casadoamor/sgca/modules/<domain>` with a consistent controller -> service -> repository layering and DTOs within each module.
- Cross-cutting infrastructure lives in `src/main/java/br/com/casadoamor/sgca/infra` (configuration, security) and shared DTOs/utilities in `src/main/java/br/com/casadoamor/sgca/modules/common`.

## Local development workflow
- On Windows run `.\mvnw.cmd clean install` or `.\mvnw.cmd test` to build and execute tests; on Unix-like systems use `./mvnw`.
- For a focused test run use `.\mvnw.cmd -Dtest=ClassName test` to avoid booting the entire suite.
- `application.properties` pulls secrets from environment variables (`${SGCA_*}`); keep `.env` local and never commit secrets.
- The default `dev` profile expects the MySQL instance described in `README.md`; `docker-compose.yml` provisions MySQL plus the backend container when needed.

## Database seeding and profiles
- `DataSeederConfig` (`src/main/java/br/com/casadoamor/sgca/infra/config`) seeds permissions, roles, and the admin account when the active Spring profile is `dev` or `test` and `app.seed.enabled=true`.
- Tests disable seeding through `app.seed.enabled=false` in `application-test.properties` and override the `seedDatabase` bean with `TestDataSeederOverrideConfig` to keep the H2 database clean.
- Persist long-lived data via Flyway migrations in `src/main/resources/db/migration`; reserve the seeder for transient dev/test fixtures only.

## Architectural conventions
- Controllers expose REST endpoints under `/auth` and `/api/...`, remain thin, and delegate to services. Services centralise business rules and call repositories plus helpers (`CpfUtil`, `PasswordValidator`, `JwtUtil`, etc.).
- Always expose DTOs to clients instead of entities; reuse builders like `MessageResponseDTO.success(String)` for simple responses.
- Entities rely on Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`). Prefer `@RequiredArgsConstructor` for dependency injection in services and components.
- CPF is the canonical username; always normalise input with `CpfUtil.limparCpf` before lookups. Enforce password strength via `PasswordValidator.validarOuLancarExcecao` and update `HistoricoSenhaService` when passwords change.
- Respect the two-factor flow by returning `AuthResponseDTO` with `requires2FA=true` when `TwoFactorService` indicates an extra step is required.

## Testing guidelines
- Tests reside under `src/test/java/br/com/casadoamor/sgca` and mirror the main package layout (for example `service/auth`). Mockito is the default mocking tool; initialise with `MockitoAnnotations.openMocks(this)` or use JUnit 5 extensions.
- The H2 profile uses `spring.jpa.hibernate.ddl-auto=create-drop`; do not rely on seed data and set up state explicitly within each test.
- Mock repository results with `Optional.of(...)` or `Optional.empty()` to match repository signatures, and verify side effects such as audit logging or session creation.
- Run `.\mvnw.cmd test` before delivering changes. Jacoco is not configured; add instrumentation only when explicitly requested.

## Logging and error handling
- Use Lombok `@Slf4j` for logging. Provide useful context (CPF, email, IP) but never log secrets or raw passwords.
- Throw domain-specific exceptions where they exist, or `RuntimeException` with user-friendly messages. Let the global exception handlers in `infra` translate errors instead of crafting ad-hoc responses.

## Security pointers
- `JwtUtil` issues tokens and returns expiration in milliseconds. `SessaoService` persists active sessions and must be updated whenever token issuance logic changes.
- `HistoricoSenhaService` prevents password reuse; ensure both the hash and history table update on password changes.
- `AuditoriaService` handles rate limiting and lockout audit trails. When adjusting auth flows, call `registrarLoginSucesso` or `registrarLoginFalha` with the correct context.

## Reference material
- `README.md` for environment setup, API overview, and security checklist.
- `DATABASE_SEEDING_GUIDE.md` for guidance on choosing between Flyway and Java-based seeding.
- `PERMISSIONS_API_GUIDE.md` and related markdown files for domain behaviour expectations.

Follow these conventions to keep new features consistent and to avoid reintroducing seeding or configuration regressions.
