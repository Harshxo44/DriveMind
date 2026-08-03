# Guardian Backend

Java 21 / Spring Boot backend for Guardian, an aftermarket automotive co-driver.

## First endpoint

`GET /api/health` returns the service status and is intentionally public. All other endpoints will require authentication when added.

## Run locally

1. Install a Java 21 JDK and Maven.
2. Open this folder in VS Code.
3. In the integrated terminal, run `mvn spring-boot:run`.
4. Visit `http://localhost:8080/api/health`.

Expected response:

```json
{
  "status": "ok",
  "service": "guardian-backend"
}
```

The default `local` profile uses an in-memory H2 database so the project starts immediately. Later, use environment variables and the `postgres` profile to connect to PostgreSQL.
