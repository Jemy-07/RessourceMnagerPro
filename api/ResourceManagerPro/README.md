# Resource Manager Pro — Backend

Spring Boot backend for **Resource Manager Pro**, built with **Clean / Hexagonal Architecture**.

## Stack
- Spring Boot 3.3.x, Java 17, Maven
- MariaDB + Flyway migrations
- Spring Security + JWT (auth module), Firebase FCM (push)
- MapStruct, Lombok, springdoc-openapi
- Testcontainers (MariaDB) for integration tests

## Architecture

Dependency rule — arrows point **inward only**:

```
web → application → domain
infrastructure → application / domain
```

| Layer            | Allowed | Forbidden |
|------------------|---------|-----------|
| `domain`         | pure Java business rules | Spring, JPA, web |
| `application`    | use cases, `@Service`/`@Transactional` | JPA, web |
| `infrastructure` | JPA entities, adapters, security, FCM, config | — |
| `web`            | REST controllers, Request/Response models | exposing domain/JPA |

### Package-by-feature shape
Every feature repeats:

```
feature/
  domain/          (model, vo)
  application/     (port/in, port/out, usecase, dto — Command/Result)
  infrastructure/  (persistence: JpaEntity, Spring-Data repo, mapper, adapter)
  web/             (controller, request, response, web mapper — Request/Response)
```

Base package: `com.cuea.rmp`. Shared cross-cutting code lives under `com.cuea.rmp.common`.

## Prerequisites
- JDK 17
- MariaDB running on `localhost:3306`
- Maven (or use the bundled `./mvnw`)

## Configuration
Defaults live in `src/main/resources/application.yml`. Override via environment variables:

| Variable          | Default            | Notes |
|-------------------|--------------------|-------|
| `DB_USERNAME`     | `root`             | MariaDB user |
| `DB_PASSWORD`     | `Josh@977`         | MariaDB password |
| `APP_JWT_SECRET`  | dev placeholder    | **must** be overridden in production |

The datasource URL uses `createDatabaseIfNotExist=true`, so the `rmp` database is
created automatically on first run.

## Run

```bash
# default profile is 'dev'
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

## Verify

```bash
curl http://localhost:8080/actuator/health
# -> {"status":"UP", ...}
```

API docs (once endpoints exist): http://localhost:8080/swagger-ui.html

## Test

```bash
./mvnw test
```
