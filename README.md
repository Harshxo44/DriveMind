# Velora

> **Intelligence in motion.**

Velora is an automotive intelligence platform that turns an ordinary passenger
vehicle into a connected, conversational co-driver. It combines an OBD-II
hardware device, an Android companion application, a secure cloud backend, and
an AI orchestration foundation to help drivers understand their vehicle without
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
                                                               │ AI foundation,│
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

- Vehicle-aware conversation endpoints that receive driver, vehicle, trip, and
  telemetry context.
- Deterministic intent planning, tool dispatch, response synthesis, and
  telemetry safety checks in the backend orchestration layer.
- Voice conversation support through the backend voice service abstraction.
- A mock voice provider for local STT/TTS pipeline development and testing.

## AI/ML architecture

Velora is being developed as a modular automotive AI pipeline rather than as a
single general-purpose chatbot. The backend currently provides the orchestration
and safety foundation; retrieval, trained models, and local inference are
planned extensions.

```text
User query
    ↓
Speech-to-text / text input
    ↓
Intent and query router
    ↓
┌─────────────────────────────────────────────┐
│ Automotive RAG │ ML models │ Tools │ LLM    │
└─────────────────────────────────────────────┘
    ↓
Context + vehicle telemetry
    ↓
Local LLM / small language model
    ↓
Safety and response validation
    ↓
Voice / text response
```

### Implemented AI foundation

- Deterministic intent routing for vehicle health, powertrain, fuel,
  diagnostics, trip, external-information, and general queries.
- Tool-dispatch boundaries for telemetry, diagnostics, driver/trip context, and
  automotive knowledge.
- Grounded response synthesis from request context and live telemetry payloads.
- Deterministic safety checks for critical coolant temperature and battery
  voltage conditions before response generation.
- Voice service abstraction with a mock provider for local development.

These components are an orchestration foundation, not yet a production LLM,
retrieval, or machine-learning inference system.

### Automotive RAG (in development)

The planned local knowledge base will contain:

- Vehicle owner manuals
- OBD-II documentation
- Diagnostic trouble codes
- Maintenance documentation
- Component specifications
- Troubleshooting information

The target retrieval pipeline is:

```text
Documents
   ↓
Document parsing
   ↓
Chunking
   ↓
Embeddings
   ↓
FAISS vector index
   ↓
Semantic retrieval
   ↓
Relevant context
   ↓
Local LLM
   ↓
Grounded response
```

### ML and telemetry intelligence (planned)

Velora's telemetry stream provides a foundation for experiments in predictive
maintenance and driver behaviour modelling. Initial experiments are intended to
run in Google Colab, with evaluated models exported for local inference.

**Predictive maintenance**

```text
OBD-II telemetry
      ↓
Feature engineering
      ↓
Random Forest / XGBoost / anomaly detection
      ↓
Component or maintenance risk
```

**Driver behaviour and efficiency**

Candidate features include speed, RPM, acceleration, braking, trip duration,
and fuel consumption. Candidate outputs include driver classification, anomaly
detection, driving-score estimation, and fuel-efficiency prediction.

Model work will be considered implemented only after training data, evaluation
metrics, reproducible experiments, and an inference path are committed to the
repository.

### Target AI/ML architecture

```text
                         VELORA
                            │
                     User / Driver
                            │
                    Voice or Text Input
                            │
                       Whisper STT
                            │
                    Intent / Query Router
                            │
             ┌──────────────┼──────────────┐
             │              │              │
        Automotive RAG   ML Engine     Tool Layer
             │              │              │
          FAISS         Prediction      Vehicle Data
             │          & Detection      Navigation
             │              │            Diagnostics
             └──────────────┼──────────────┘
                            │
                    Context Builder
                            │
                     Local LLM / SLM
                            │
                   Safety Validation
                            │
                    Response Generator
                            │
                     Piper TTS / Text
```

### Privacy and security

- JWT-based authentication for protected API resources.
- Spring Security request protection and validation.
- Per-user data ownership in the application services.
- Privacy summary and personal-data purge endpoints.
- H2 for fast local development and PostgreSQL configuration for deployment.

## Repository layout

```text
Velora/
├── velora-backend/         # Spring Boot 3.5 Velora backend, REST API, persistence, auth
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
cd velora-backend
mvn test

:: Start local development
mvn spring-boot:run
```

Before opening a pull request:

1. Run `mvn test` from `velora-backend`.
2. Keep secrets and local database files out of commits.
3. Preserve the read-only vehicle safety boundary.
4. Update API consumers when a backend route or payload changes.
5. Verify BLE and telemetry changes against representative hardware data.

## Project status

Velora is an active prototype moving toward a complete connected-vehicle
platform. The current scope is intentionally separated by maturity:

**Implemented**

- Spring Boot backend with JWT authentication, vehicle/trip/telemetry domains,
  diagnostics, reminders, privacy endpoints, and deterministic AI
  orchestration.
- H2 local development and PostgreSQL deployment configuration.
- Android API/BLE integration surfaces and ESP32 firmware modules for BLE,
  OBD-II, telemetry, and state handling.

**In development**

- Automotive RAG with document parsing, embeddings, FAISS retrieval, and
  grounded local-LLM responses.
- Predictive-maintenance and driver-behaviour experiments using vehicle
  telemetry.
- A production voice pipeline beyond the current mock provider.

**Planned**

- Reproducible model training and evaluation datasets and pipelines.
- Exported local inference models and production hardware validation.
- Advanced computer vision and cloud synchronization.

Mobile, firmware, and AI components remain integration surfaces until their
build tooling, data pipelines, evaluation results, and device provisioning
flows are complete.


