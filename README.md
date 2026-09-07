# Velora

> **Intelligence in motion.**

Velora is an automotive intelligence platform that turns an ordinary passenger
vehicle into a connected, conversational co-driver. It combines an OBD-II
hardware device, an Android companion application, a secure cloud backend, and
an AI orchestration layer to help drivers understand their vehicle without
touching the car's control systems.

Velora is designed as a **read-only safety and awareness system**. It can
observe vehicle telemetry, explain diagnostics, remember trips, and provide
short driver-aware guidance. It does not steer, brake, accelerate, or write
commands to a vehicle CAN bus.

## Why Velora?

Many cars on the road do not provide modern connected-car features. Drivers
still need a practical way to:

- understand whether their vehicle is operating normally;
- receive useful warnings before a small issue becomes a breakdown;
- translate technical diagnostic trouble codes into plain language;
- review trips, telemetry, and driving context in one place; and
- ask natural-language questions about the vehicle while keeping attention on
  the road.

Velora addresses this gap with a distributed architecture:

```text
┌──────────────────┐   BLE   ┌──────────────────┐   REST/JWT   ┌───────────────┐
│  Vehicle OBD-II  │────────▶│ Android companion │────────────▶│ Velora backend│
│  ESP32 device    │         │ app + BLE client  │             │ Spring Boot   │
└──────────────────┘         └──────────────────┘             └───────┬───────┘
                                                                       │
                                              telemetry + context      │
                                                                       ▼
                                                               ┌───────────────┐
                                                               │ AI services,  │
                                                               │ diagnostics,  │
                                                               │ trips, alerts │
                                                               └───────────────┘
```

## Core capabilities

### Vehicle awareness

- OBD-II telemetry collection for speed, RPM, coolant temperature, throttle,
  fuel level, battery voltage, location, heading, and active DTCs.
- Recent and latest telemetry queries for vehicles and trips.
- Diagnostic trouble code lookup and vehicle health summaries.
- Vehicle registration and hardware-device pairing.

### Driver and trip intelligence

- Driver profiles with experience, tone, speed-alert, RPM-alert, and greeting
  preferences.
- Trip start, trip completion, trip history, and vehicle trip history.
- Maintenance and custom reminders with completion and deletion workflows.
- Velora alert configuration, alert history, and acknowledgement support.

### Conversational assistance

- Vehicle-aware AI conversation endpoints that receive driver, vehicle, trip,
  and telemetry context.
- Intent planning and safety policy components in the backend orchestration
  layer.
- Voice conversation support through the backend voice service abstraction.
- A mock voice provider is included for local development.

### Privacy and security

- JWT-based authentication for protected API resources.
- Spring Security request protection and validation.
- Per-user data ownership in the application services.
- Privacy summary and personal-data purge endpoints.
- H2 for fast local development and PostgreSQL configuration for deployment.

## Repository layout

```text
Velora/
├── guardian-backend/       # Spring Boot 3.5 backend, REST API, persistence, auth
├── mobile/android/         # Android BLE and Retrofit client implementation
├── firmware/               # ESP32-side BLE, OBD-II, telemetry, and state code
├── velora-ai/              # AI workspace for models, knowledge, and application code
├── .github/                # Repository automation and project configuration
└── README.md               # This overview
```

## Technology stack

| Area | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5.16 |
| Web/API | Spring MVC, Bean Validation, REST, JSON |
| Security | Spring Security, JWT (`jjwt` 0.12.6) |
| Persistence | Spring Data JPA, H2 locally, PostgreSQL runtime |
| Mobile networking | Kotlin, Retrofit |
| Vehicle connectivity | Bluetooth Low Energy, OBD-II |
| Embedded firmware | C++17-compatible ESP32-oriented modules |
| Testing | Spring Boot Test, Spring Security Test, JUnit |

## Run the backend locally

### Prerequisites

- JDK 21
- Maven 3.9+
- Git

### Windows Command Prompt

From the repository root:

```cmd
cd guardian-backend
mvn spring-boot:run
```

The local profile uses an in-memory H2 database, so no database installation
is required for the first run.

Verify the service:

```cmd
curl http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "ok",
  "service": "guardian-backend"
}
```

### Run the test suite

```cmd
cd guardian-backend
mvn test
```

### Build a runnable JAR

```cmd
cd guardian-backend
mvn clean package
java -jar target\guardian-backend-0.0.1-SNAPSHOT.jar
```

## Database profiles

The default configuration is intended for local development:

```cmd
cd guardian-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

For PostgreSQL, provide the connection settings expected by the application
and start with the PostgreSQL profile:

```cmd
cd guardian-backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Do not commit passwords, signing keys, or production connection strings.
Provide secrets through environment variables or the deployment environment.

## API surface

The backend exposes a health endpoint at `/api/health`. Versioned application
resources are rooted at `/api/v1` and include:

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login
GET    /api/v1/drivers/profile
PUT    /api/v1/drivers/profile
GET    /api/v1/vehicles
POST   /api/v1/vehicles
POST   /api/v1/devices
POST   /api/v1/devices/{deviceId}/pair
POST   /api/v1/trips/start
POST   /api/v1/trips/{tripId}/end
GET    /api/v1/trips/my
POST   /api/v1/telemetry/ingest
GET    /api/v1/telemetry/vehicle/{vehicleId}/latest
GET    /api/v1/diagnostics/dtc/{code}
GET    /api/v1/diagnostics/health/{vehicleId}
POST   /api/v1/ai/conversation
POST   /api/v1/voice/conversation
GET    /api/v1/reminders
POST   /api/v1/reminders
GET    /api/v1/privacy/summary
DELETE /api/v1/privacy/purge-my-data
```

Protected routes use a bearer token:

```cmd
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" ^
     http://localhost:8080/api/v1/vehicles
```

## Mobile and hardware integration

The Android client contains the first integration layer for the physical
device:

- Retrofit methods for authentication, vehicles, devices, trips, telemetry,
  diagnostics, AI conversation, reminders, and privacy.
- BLE GATT connection management for the Velora device.
- Telemetry notifications using the configured service and characteristic UUIDs.
- Binary telemetry packet parsing into Kotlin telemetry models.

The firmware contains focused modules for:

- BLE GATT serving;
- OBD-II PID discovery and parsing;
- engine-state transitions; and
- telemetry packet encoding.

The Android and firmware components are currently integration surfaces within
the repository. Their build tooling and device provisioning flow should be
completed before treating them as release-ready applications.

## Safety boundaries

Velora is not an autonomous-driving system and is not a replacement for a
qualified mechanic. The V1 design intentionally excludes:

- steering, braking, throttle, or cruise-control actuation;
- writing manufacturer-specific commands to vehicle networks;
- autonomous driving or advanced driver assistance;
- a dedicated cellular modem in the device; and
- safety-critical decisions that require professional inspection.

Drivers must follow local traffic laws and should only interact with Velora
when it is safe to do so.

## Development workflow

```cmd
:: Clone
git clone https://github.com/Harshxo44/Velora.git
cd Velora

:: Check the backend
cd guardian-backend
mvn test

:: Start local development
mvn spring-boot:run
```

Before opening a pull request:

1. Run `mvn test` from `guardian-backend`.
2. Keep secrets and local database files out of commits.
3. Preserve the read-only vehicle safety boundary.
4. Update API consumers when a backend route or payload changes.
5. Verify BLE and telemetry changes against representative hardware data.

## Project status

Velora is an active prototype moving toward a complete connected-vehicle
platform. The backend already provides the central domain model and API
foundation; mobile, firmware, and AI components are being integrated around
that foundation.

## License

No license has been declared yet. Add a project license before distributing
Velora or accepting external contributions.
