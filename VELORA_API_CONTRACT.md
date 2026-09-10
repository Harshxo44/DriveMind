# Guardian V1 — API & Data Contracts Specification

> **Document Status:** FROZEN / BASELINE V1 CONTRACTS  
> **Backend Service:** Spring Boot (`velora-backend`)  
> **Base URL:** `http://localhost:8080/api/v1` (Local) / `https://api.drivemind.ai/api/v1` (Cloud)  
> **Authentication:** Bearer JWT in `Authorization: Bearer <token>`  

---

## Table of Endpoints

| Category | Method | Path | Summary | Auth Required |
|---|---|---|---|---|
| **Health** | `GET` | `/api/health` | System health check | ❌ Public |
| **Auth** | `POST` | `/api/v1/auth/register` | Register new driver account | ❌ Public |
| **Auth** | `POST` | `/api/v1/auth/login` | Login and obtain JWT token | ❌ Public |
| **Driver Profile** | `GET` | `/api/v1/drivers/profile` | Get driver profile & preferences | ✅ Yes |
| **Driver Profile** | `PUT` | `/api/v1/drivers/profile` | Update speed thresholds & tone | ✅ Yes |
| **Vehicles** | `GET` | `/api/v1/vehicles` | List all vehicles owned by user | ✅ Yes |
| **Vehicles** | `POST` | `/api/v1/vehicles` | Register vehicle (VIN, make, model) | ✅ Yes |
| **Vehicles** | `GET` | `/api/v1/vehicles/{id}` | Get vehicle details | ✅ Yes |
| **Devices** | `POST` | `/api/v1/devices` | Register Guardian hardware serial | ✅ Yes |
| **Devices** | `POST` | `/api/v1/devices/{id}/pair` | Pair hardware device to vehicle | ✅ Yes |
| **Devices** | `POST` | `/api/v1/devices/{id}/unpair`| Unpair hardware device | ✅ Yes |
| **Devices** | `POST` | `/api/v1/devices/{id}/heartbeat`| BLE/Wi-Fi active heartbeat ping | ✅ Yes |
| **Trips** | `POST` | `/api/v1/trips/start` | Start live trip recording | ✅ Yes |
| **Trips** | `POST` | `/api/v1/trips/{id}/end` | End trip & calculate safety score | ✅ Yes |
| **Trips** | `GET` | `/api/v1/trips/my` | Get driver's trip history | ✅ Yes |
| **Telemetry** | `POST` | `/api/v1/telemetry/ingest` | Ingest real-time OBD snapshot | ✅ Yes |
| **Telemetry** | `GET` | `/api/v1/telemetry/vehicle/{id}/latest`| Get newest telemetry record | ✅ Yes |
| **Diagnostics** | `GET` | `/api/v1/diagnostics/dtc/{code}` | Lookup Diagnostic Trouble Code | ✅ Yes |
| **Diagnostics** | `GET` | `/api/v1/diagnostics/health/{id}` | Aggregate vehicle health score | ✅ Yes |
| **AI Orchestrator**| `POST` | `/api/v1/ai/conversation` | Vehicle-aware LLM query engine | ✅ Yes |
| **Reminders** | `GET` | `/api/v1/reminders` | List maintenance/custom reminders | ✅ Yes |
| **Reminders** | `POST` | `/api/v1/reminders` | Create new reminder | ✅ Yes |
| **Reminders** | `PUT` | `/api/v1/reminders/{id}/complete` | Mark reminder done | ✅ Yes |
| **Reminders** | `DELETE`| `/api/v1/reminders/{id}` | Delete reminder | ✅ Yes |
| **Privacy (DPDPA)**| `GET` | `/api/v1/privacy/summary` | Summary of stored user data | ✅ Yes |
| **Privacy (DPDPA)**| `DELETE`| `/api/v1/privacy/purge-my-data`| Permanently wipe all telemetry | ✅ Yes |

---

## 1. Authentication Endpoints

### 1.1 Driver Registration
- **Endpoint:** `POST /api/v1/auth/register`
- **Request Body:**
```json
{
  "name": "Harsh",
  "email": "harsh@drivemind.ai",
  "phone": "+919876543210",
  "password": "securePassword123"
}
```
- **Response (201 Created):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "harsh@drivemind.ai",
  "name": "Harsh",
  "role": "ROLE_DRIVER"
}
```

### 1.2 Driver Login
- **Endpoint:** `POST /api/v1/auth/login`
- **Request Body:**
```json
{
  "email": "harsh@drivemind.ai",
  "password": "securePassword123"
}
```
- **Response (200 OK):** (Same schema as Registration)

---

## 2. Driver Profile Endpoints

### 2.1 Get Driver Profile
- **Endpoint:** `GET /api/v1/drivers/profile`
- **Response (200 OK):**
```json
{
  "id": "e4a2c1f0-...",
  "userId": "3fa85f64-...",
  "displayName": "Harsh",
  "experienceLevel": "INTERMEDIATE",
  "tonePreference": "CONCISE",
  "speedAlertThresholdKmh": 80.0,
  "highRpmThreshold": 3500,
  "voiceGreetingEnabled": true,
  "updatedAt": "2026-09-02T14:30:00Z"
}
```

### 2.2 Update Driver Profile
- **Endpoint:** `PUT /api/v1/drivers/profile`
- **Request Body:**
```json
{
  "displayName": "Harsh",
  "experienceLevel": "EXPERIENCED",
  "tonePreference": "CALM",
  "speedAlertThresholdKmh": 90.0,
  "highRpmThreshold": 4000,
  "voiceGreetingEnabled": true
}
```

---

## 3. Vehicle & Device Pairing

### 3.1 Register Vehicle
- **Endpoint:** `POST /api/v1/vehicles`
- **Request Body:**
```json
{
  "make": "Hyundai",
  "model": "i20",
  "year": "2021",
  "vin": "MALBB51BLAM123456",
  "registrationNumber": "DL 01 AB 1234",
  "obdProtocol": "ISO 15765-4 CAN"
}
```

### 3.2 Pair Guardian Device
- **Endpoint:** `POST /api/v1/devices/{deviceId}/pair`
- **Request Body:**
```json
{
  "vehicleId": "8b9e6c1a-..."
}
```

---

## 4. Telemetry Ingestion (Phone ➔ Cloud)

- **Endpoint:** `POST /api/v1/telemetry/ingest`
- **Request Body:**
```json
{
  "vehicleId": "8b9e6c1a-...",
  "tripId": "f7d2b4a1-...",
  "speedKmh": 68.4,
  "rpm": 2240,
  "engineCoolantTempC": 91.5,
  "throttlePositionPct": 22.0,
  "fuelLevelPct": 45.0,
  "batteryVoltageV": 14.1,
  "latitude": 28.6139,
  "longitude": 77.2090,
  "heading": 180.0,
  "activeDtcCodes": "",
  "timestamp": "2026-09-02T14:35:12Z"
}
```

---

## 5. AI Conversation Orchestrator

- **Endpoint:** `POST /api/v1/ai/conversation`
- **Request Body:**
```json
{
  "systemInstruction": "You are Guardian, an in-car AI co-driver. Keep answers under 2 sentences. Prioritize driver safety.",
  "driver": {
    "name": "Harsh",
    "experienceLevel": "intermediate",
    "tonePreference": "concise"
  },
  "vehicle": {
    "make": "Hyundai",
    "model": "i20",
    "year": 2021,
    "fuelType": "Petrol"
  },
  "telemetry": {
    "speedKmh": 68.0,
    "rpm": 2200,
    "coolantTempC": 91.0,
    "batteryVoltage": 14.1,
    "fuelLevelPct": 42.0,
    "throttlePositionPct": 20.0,
    "dtcActive": []
  },
  "trip": {
    "durationMinutes": 24,
    "distanceKm": 16.4,
    "startLocation": "Home",
    "destination": "Office"
  },
  "ephemeralMemory": [
    "Driver mentioned buying groceries on the way home"
  ],
  "userQuery": "How is the car doing right now?"
}
```

- **Response (200 OK):**
```json
{
  "spokenResponse": "Your coolant temperature is 91 degrees and battery is 14.1 volts. Everything looks healthy in your Hyundai i20.",
  "displaySummary": "All vehicle systems nominal. Coolant: 91°C, Battery: 14.1V, RPM: 2200",
  "category": "VEHICLE_HEALTH",
  "criticalAlert": false,
  "suggestedFollowUps": [
    "What is my current fuel range?",
    "Check active fault codes"
  ],
  "timestamp": "2026-09-02T14:35:14Z"
}
```

---

## 6. Reminders API

### 6.1 Create Reminder
- **Endpoint:** `POST /api/v1/reminders`
- **Request Body:**
```json
{
  "vehicleId": "8b9e6c1a-...",
  "title": "Engine Oil Replacement (10,000 km)",
  "reminderType": "MAINTENANCE",
  "dueOdometerKm": 10000.0,
  "dueDate": "2026-10-01T00:00:00Z",
  "notes": "5W-30 Synthetic oil and OEM filter"
}
```

### 6.2 Mark Reminder Complete
- **Endpoint:** `PUT /api/v1/reminders/{reminderId}/complete`

---

## 7. Privacy & Data Deletion (DPDPA 2023 Compliance)

### 7.1 Data Retention Summary
- **Endpoint:** `GET /api/v1/privacy/summary`
- **Response (200 OK):**
```json
{
  "registeredVehicles": 1,
  "recordedTrips": 12,
  "storedReminders": 3,
  "dataRetentionPolicy": "DPDPA 2023 Compliant: Telemetry purges on request; In-trip conversation RAM purges on engine off."
}
```

### 7.2 Purge All User Telemetry & Trips
- **Endpoint:** `DELETE /api/v1/privacy/purge-my-data`
- **Response (200 OK):**
```json
{
  "status": "SUCCESS",
  "message": "All telemetry, trip logs, alerts, and reminders permanently erased.",
  "deletedSnapshots": 1420,
  "deletedTrips": 12,
  "deletedAlerts": 2,
  "deletedReminders": 3
}
```
