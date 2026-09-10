# Guardian V1 — Authoritative Technical Specification

> **Document Status:** FROZEN / BASELINE V1 SPECIFICATION  
> **Target Release:** Prototype V1 (Physical Hardware + Mobile App + Cloud AI Backend)  
> **Market Focus:** India-First (Mid-range & older passenger vehicles)  
> **Date:** September 2026  

---

## Table of Contents
1. [Executive Summary & Core Value Proposition](#1-what-guardian-does)
2. [Explicit Out-of-Scope Guardrails (What Guardian Does NOT Do)](#2-what-guardian-does-not-do)
3. [Target Cars & Vehicle Compatibility](#3-target-cars)
4. [Target Users & Personas](#4-target-users)
5. [Hardware Architecture & Component Selection](#5-hardware-required)
6. [OBD-II Telemetry & Parameter ID (PID) Specification](#6-obd-ii-data-required)
7. [Device ↔ Phone Communication Protocol (BLE / Wi-Fi)](#7-phone--guardian-communication)
8. [Phone ↔ Cloud Communication Protocol (REST / WebSockets)](#8-phone--cloud-communication)
9. [AI / LLM Orchestration & Context Architecture](#9-aillm-architecture)
10. [Voice Pipeline: Audio Capture, STT & TTS](#10-voicestttts-architecture)
11. [Driver Identification & Profile System](#11-driver-identificationprofile-system)
12. [Privacy Layer & Ephemeral Trip Memory](#12-private-conversation--temporary-memory-system)
13. [Smart Proactive Reminders Engine](#13-reminders)
14. [Vehicle Diagnostics & Health Monitoring](#14-diagnostics)
15. [Security, Encryption & Regulatory Compliance](#15-security--encryption)
16. [Offline Operation & Graceful Degradation](#16-offline-behavior)
17. [Hardware Bill of Materials (BOM) & Target Cost](#17-hardware-cost-target)
18. [Performance & Latency Budgets](#18-performance-requirements)
19. [Testing Strategy, Validation Gates & Compatibility Matrix](#19-testingacceptance-criteria)

---

## 1. What Guardian Does

Guardian is an **aftermarket, AI-powered conversational co-driver** designed to retrofit intelligence and vehicle awareness into passenger cars that lack modern connected features.

### Core Capabilities for V1
- **Driver Recognition & Contextual Greeting:** Detects driver entry via BLE beacon / phone handshake and delivers a voice greeting personalized to the driver profile.
- **Real-Time Telemetry Monitoring:** Continuously samples essential engine, electrical, and operational metrics via standard OBD-II (ELM327/STN transceiver).
- **Conversational Vehicle Intelligence:** Leverages an LLM orchestrator injected with live telemetry to answer natural language queries (e.g., *"How is the engine running?"*, *"Why is my fuel efficiency dropping?"*, *"What does this warning light mean?"*).
- **Proactive Voice Alerts & Warnings:** Detects threshold breaches (e.g., coolant overheat, low battery voltage, aggressive over-revving) and proactively speaks short, glanceable warnings without requiring driver touch.
- **Intelligent Diagnostics (DTC):** Reads Diagnostic Trouble Codes, translates cryptic alphanumeric DTCs (e.g., `P0300`) into plain-language mechanical explanations, and assesses urgency.
- **Smart Reminders:** Automatically alerts drivers based on time, mileage, or trip count (e.g., *"Engine oil service is due in 350 km"*, *"Check tire pressure before highway run"*).
- **Trip Journaling & Telemetry Logs:** Summarizes completed trips (distance, duration, average speed, estimated fuel, driving behavior score) accessible via the mobile companion app.

---

## 2. What Guardian Does NOT Do

To maintain passenger safety, regulatory compliance, and cost control, the following capabilities are **strictly out of scope for V1**:

| Category | Excluded Capability | Rationale |
|---|---|---|
| **Actuation** | Direct vehicle control (steering, braking, throttle, cruise control) | High functional safety risk (ISO 26262); Guardian is strictly read-only. |
| **Comfort Actuation** | Power windows, sunroof, central locking, headlight switching | Requires intrusive CAN write commands; risks vehicle BCM tampering. |
| **Autonomous Driving** | ADAS, automated lane-keeping, automated parking | Beyond V1 compute scope; requires specialized camera/radar suites. |
| **Vision Hardware** | Dashcam video streaming, interior eye-tracking cameras | Increases hardware BOM and power/thermal envelope beyond V1 target. |
| **Stand-alone Cellular** | Dedicated on-device 4G/LTE modem and e-SIM subscription | Eliminates recurring hardware/carrier costs by utilizing driver's smartphone. |
| **Proprietary CAN Hacking** | Decoding manufacturer-specific encrypted CAN buses | V1 strictly uses standard SAE J1979 / OBD-II standard PIDs. |

---

## 3. Target Cars

Guardian V1 targets the vast majority of mid-range, compact, and older passenger vehicles operating in India and emerging markets.

### Vehicle Eligibility Criteria
- **OBD-II Compliance:** Standard 16-pin J1962 connector present (mandatory for Indian passenger cars manufactured post-April 2010 under BS-IV / BS-VI mandates).
- **Supported Protocols:**
  1. ISO 15765-4 (CAN 11-bit / 29-bit, 250 kbps / 500 kbps) — ~90% of target cars.
  2. ISO 14230-4 (KWP2000 fast init / 5 baud).
  3. ISO 9141-2 (older Asian/European models).
- **Target Manufacturers (V1 Validation Cohort):**
  - **Maruti Suzuki:** Swift, Baleno, Dzire, WagonR, Brezza (2012–2024)
  - **Hyundai:** i10, i20, Creta, Verna (2012–2024)
  - **Tata Motors:** Nexon, Tiago, Altroz, Punch (2016–2024)
  - **Kia:** Seltos, Sonet (2019–2024)
  - **Honda:** City, Amaze, Jazz (2013–2024)

---

## 4. Target Users

1. **Daily Urban Commuters:** Commuters who spend 1–3 hours daily in traffic and need proactive vehicle health updates, battery warnings, and hands-free assistance.
2. **Owners of Pre-Owned / Older Vehicles (3–12 Years Old):** Vehicles out of OEM warranty where drivers lack access to modern Apple CarPlay/Android Auto telematics or connected car apps.
3. **Budget & Value-Conscious Car Owners:** Drivers looking for connected vehicle intelligence without spending ₹40,000–₹80,000 on aftermarket touchscreen head units.
4. **Safety-Conscious Family Drivers:** Owners who share vehicles among family members and want trip logging, battery health tracking, and simplified breakdown guidance.

---

## 5. Hardware Required

To hit the target price point while maintaining high audio quality and thermal resilience, V1 adopts a **distributed edge architecture**:

```
 ┌─────────────────────────────────────────────────────────────┐
 │                    GUARDIAN V1 HARDWARE                     │
 │                                                             │
 │   ┌──────────────┐     ┌──────────────┐    ┌────────────┐   │
 │   │  Microphone  │     │ 3W Acoustic  │    │ Multi-LED  │   │
 │   │  Array (I2S) │     │ Speaker (I2S)│    │ Indicator  │   │
 │   └──────┬───────┘     └──────▲───────┘    └─────▲──────┘   │
 │          │                    │                  │          │
 │   ┌──────▼────────────────────┴──────────────────┴──────┐   │
 │   │               ESP32-S3 Dual-Core MCU                │   │
 │   │         (Wi-Fi 802.11 b/g/n + BLE 5.0 Mesh)         │   │
 │   └──────────────────────────▲──────────────────────────┘   │
 │                              │ UART                         │
 │   ┌──────────────────────────▼──────────────────────────┐   │
 │   │       Automotive OBD-II Transceiver (STN1110 /      │   │
 │   │         ELM327 v2.2 Compatible CAN Controller)      │   │
 │   └──────────────────────────▲──────────────────────────┘   │
 │                              │ 12V / ISO J1962              │
 └──────────────────────────────┼──────────────────────────────┘
                                │
                        Vehicle OBD-II Port
```

### Component Breakdown
1. **Primary Processor:** ESP32-S3-WROOM-1 (Dual-core Xtensa 32-bit LX7 @ 240 MHz, 8MB PSRAM, 8MB Flash).
2. **OBD-II / CAN Interface:** STN1110 / ELM327-compatible OBD interpreter communicating with ESP32 via UART (baud: 115200 or 38400).
3. **Audio Capture:** Dual MEMS microphone array (e.g., INMP441 or ICS-43434) with acoustic echo cancellation (AEC) and noise suppression.
4. **Audio Output:** MAX98357A I2S Class-D mono amplifier paired with a 3W 4Ω automotive-rated compact speaker.
5. **Power Management:** LM2596 / MP1584 buck converter stepping down 12V/24V vehicle battery input to 5V/3.3V with over-voltage, reverse-polarity, and load-dump protection.
6. **Physical Interface:** Capacitive mute/privacy button, emergency reset pinhole, multi-color RGB status LED (Bluetooth, OBD, Alert states).

---

## 6. OBD-II Data Required

Guardian queries standard **SAE J1979 Mode 01** Parameter IDs (PIDs) using non-invasive read commands.

### Real-Time Ingestion Matrix
| Parameter | Mode / PID | Unit / Range | Sampling Cadence | Use Case |
|---|---|---|---|---|
| **Engine RPM** | `01 0C` | 0 – 8,000 RPM | 1 Hz (1 sec) | Driving style, over-rev alerts |
| **Vehicle Speed** | `01 0D` | 0 – 255 km/h | 1 Hz (1 sec) | Speeding warnings, trip velocity |
| **Coolant Temp** | `01 05` | -40°C to +215°C | 0.1 Hz (10 sec) | Engine overheat proactive alerts |
| **Battery Voltage** | `AT RV` | 0 – 18.0 V | 0.2 Hz (5 sec) | Alternator health, low battery warning |
| **Fuel Tank Level** | `01 2F` | 0 – 100 % | 0.033 Hz (30 sec) | Low fuel reminder, range estimation |
| **Throttle Position**| `01 11` | 0 – 100 % | 1 Hz (1 sec) | Aggressive acceleration detection |
| **Diagnostic Codes**| `03` (Mode 03)| Alphanumeric list | On startup + on error | DTC malfunction lookup & explanation |
| **Ambient Temp** | `01 46` | -40°C to +215°C | 0.033 Hz (30 sec) | Weather/climate context |

*Note: If a specific vehicle does not support an optional PID (e.g. `01 2F`), Guardian flags it as unavailable in the vehicle profile and skips it without interrupting the telemetry pipeline.*

---

## 7. Phone ↔ Guardian Communication

Guardian uses **Bluetooth Low Energy (BLE 5.0)** for continuous data streaming and device control, with an optional local Wi-Fi SoftAP fallback for high-throughput firmware updates.

```
┌──────────────────┐               BLE GATT Pipeline              ┌──────────────────┐
│  Guardian Device │ ◄══════════════════════════════════════════► │  Companion App   │
│  (ESP32-S3 Edge) │   Service UUID: 6E400001-B5A3-F393-...       │ (Android/iOS)    │
└──────────────────┘                                              └──────────────────┘
```

### BLE Custom GATT Profile
- **Guardian Service UUID:** `6E400001-B5A3-F393-E0A9-E50E24DCCA9E`
- **Telemetry Characteristic (Notify):** `6E400002-B5A3-F393-E0A9-E50E24DCCA9E`
  - Compact binary payload (16 bytes per tick) containing: `[Timestamp(4B), Speed(1B), RPM(2B), Coolant(1B), Voltage(2B), Fuel(1B), Throttle(1B), StatusFlags(4B)]`.
- **Command Characteristic (Write):** `6E400003-B5A3-F393-E0A9-E50E24DCCA9E`
  - Sends configuration payloads (mute state, volume, sleep timer, driver token).
- **Audio/Event Characteristic (Indicate):** `6E400004-B5A3-F393-E0A9-E50E24DCCA9E`
  - Alerts device when cloud voice output is streaming back to phone speaker/Guardian speaker.

---

## 8. Phone ↔ Cloud Communication

The companion mobile app acts as the internet gateway between the vehicle and the backend cloud.

```
┌──────────────────┐         HTTPS / REST + WSS (JWT Auth)         ┌──────────────────┐
│  Companion App   │ ◄══════════════════════════════════════════► │ Guardian Backend │
│  (Android Gateway│                                              │ (Spring Boot)    │
└──────────────────┘                                              └──────────────────┘
```

### API Protocols & Endpoints
- **Transport:** HTTPS (TLS 1.3) + Secure WebSockets (`wss://`) for live AI audio/chat streaming.
- **Authentication:** Bearer JWT in `Authorization` header with automatic refresh token rotation.
- **Key REST Routes:**
  - `POST /api/v1/auth/login` & `POST /api/v1/auth/register`
  - `POST /api/v1/device/pair` (bind device MAC to user profile)
  - `POST /api/v1/telemetry/snapshot` (batched offline/online sync, 30-sec windows)
  - `POST /api/v1/trips/start` & `POST /api/v1/trips/end`
  - `GET  /api/v1/diagnostics/health-summary/{vehicleId}`
  - `POST /api/v1/ai/conversation` (structured prompt orchestrator)

---

## 9. AI / LLM Architecture

Guardian's AI engine is **strictly grounded in live vehicle state** rather than functioning as a generic chatbot.

```
 Driver Voice Input ──► STT ──► Prompt Context Builder ──► LLM Orchestrator ──► TTS ──► Voice Audio
                                         ▲
                                         │
                   ┌─────────────────────┴─────────────────────┐
                   │ Structured Live Context Payload:          │
                   │ • Driver Profile (Name, Style)            │
                   │ • Vehicle Telemetry (Speed, RPM, Coolant) │
                   │ • Active DTCs / Faults                    │
                   │ • Trip State (Duration, Distance)         │
                   │ • Temporary In-Memory Facts               │
                   └───────────────────────────────────────────┘
```

### Context Injection Schema (JSON)
```json
{
  "system_instruction": "You are Guardian, an in-car AI co-driver. Keep answers under 2 sentences. Prioritize driver safety. Tone is calm, concise, and helpful.",
  "driver": {
    "name": "Harsh",
    "experience_level": "intermediate",
    "tone_preference": "concise"
  },
  "vehicle": {
    "make": "Hyundai",
    "model": "i20",
    "year": 2021,
    "fuel_type": "Petrol"
  },
  "telemetry": {
    "speed_kmh": 68,
    "rpm": 2200,
    "coolant_temp_c": 92,
    "battery_voltage": 14.1,
    "fuel_level_pct": 42,
    "dtc_active": []
  },
  "trip": {
    "duration_minutes": 24,
    "distance_km": 16.4
  },
  "ephemeral_memory": [
    "Driver mentioned buying groceries on the way home"
  ],
  "user_query": "How is the engine doing right now?"
}
```

---

## 10. Voice / STT / TTS Architecture

To maintain low latency (<1.5s total turnaround) and avoid unnecessary hardware cost:

```
┌────────────────┐      Audio Stream     ┌────────────────┐    Cloud STT     ┌────────────────┐
│ Guardian Mic / │ ────────────────────► │  Phone App     │ ───────────────► │ Cloud Voice    │
│ Phone Mic      │                       │ (Preprocessing)│                  │ Pipeline       │
└────────────────┘                       └────────────────┘                  └──────┬─────────┘
                                                                                    │
                                                                                    ▼
┌────────────────┐      Play Audio       ┌────────────────┐    Synthesize    ┌────────────────┐
│ Speaker Output │ ◄──────────────────── │  Phone App     │ ◄─────────────── │ Cloud TTS      │
│ (Car/Guardian) │                       │ (Audio Player) │                  │ (Deepgram/     │
└────────────────┘                       └────────────────┘                  │  ElevenLabs)   │
```

1. **Wake-Word Detection:** On-device / Mobile wake-word engine (e.g., *"Hey Guardian"* via Porcupine or lightweight local TFLite model).
2. **Speech-to-Text (STT):** Deepgram Nova-2 / Whisper API via Mobile Gateway for sub-300ms transcription with Indian English / Hindi accent support.
3. **LLM Reasoning:** Anthropic Claude 3.5 Haiku / Sonnet (or lightweight fast LLM endpoint) with structured system prompt.
4. **Text-to-Speech (TTS):** Low-latency neural TTS (Deepgram Aura / Cartesia / ElevenLabs Turbo) returning high-fidelity PCM audio.
5. **Output Routing:** Streamed directly through the vehicle's speakers via phone Bluetooth A2DP or the Guardian hardware 3W internal speaker.

---

## 11. Driver Identification / Profile System

Guardian supports multiple drivers sharing a single vehicle without requiring manual UI configuration:

- **Automatic Driver Handshake:** The phone app detects the user's secure cryptographic token over BLE when entering the car.
- **Voice Profile Confirmation (Secondary):** *"Good morning Harsh, starting your trip."* If a different voice responds, Guardian prompts: *"Should I switch profile to Priya?"*
- **Stored Profile Attributes:**
  - Driver Name & Preferred Nickname
  - Driving Behavior Baseline (Gentle vs. Spirited)
  - Preferred Warning Thresholds (e.g., speed alert at 80 km/h vs. 100 km/h)
  - Commute Presets & Work/Home Locations

---

## 12. Private Conversation & Temporary-Memory System

Privacy in private passenger cabins is critical. Guardian implements a **two-tier memory model**:

1. **Permanent Long-Term Store (Cloud DB):**
   - Vehicle maintenance records, historical DTCs, lifetime odometer, driver preferences, aggregated trip summaries.
2. **Ephemeral In-Trip Memory (RAM Only):**
   - Transient conversational context (e.g., *"Remind me to stop at the ATM"*, *"We need to call Mom when we arrive"*).
   - **Auto-Purge Guarantee:** All temporary conversation transcripts and in-trip working memory are **permanently wiped upon trip termination (`Engine Off` / `Trip Ended`)**.
   - **Manual Privacy Button:** A hardware/app button immediately purges current memory and mutes audio recording with an LED confirmation.

---

## 13. Reminders

Guardian includes an automated, time- and telemetry-triggered reminder engine:

- **Metric-Triggered Maintenance Reminders:**
  - Engine Oil Change (e.g., every 10,000 km or 12 months)
  - Brake Fluid & Coolant Flush (every 24 months / 30,000 km)
  - Battery Health Check (triggered if resting voltage < 11.9V)
- **Contextual Driving Reminders:**
  - Continuous driving duration > 2.5 hours $\rightarrow$ Proactive fatigue rest reminder.
  - Fuel level < 15% $\rightarrow$ Prompt for nearby gas station.
- **User-Defined Ad-hoc Reminders:**
  - Voice-created reminders triggered on trip completion (e.g., *"Remind me to take my laptop bag when the car stops"*).

---

## 14. Diagnostics

Guardian translates diagnostic complexity into actionable safety information:

```
┌──────────────────┐
│ OBD-II Mode 03   │ ──► [ DTC Code: P0117 ]
│ (DTC Retrieval)  │
└──────────────────┘            │
                                ▼
                   ┌─────────────────────────┐
                   │ Guardian Diagnostics DB │
                   │ Severity: HIGH (Red)    │
                   │ System: Engine Cooling  │
                   └────────────┬────────────┘
                                │
                                ▼
                   ┌──────────────────────────────────────────────┐
                   │ AI Explanation:                              │
                   │ "Your Engine Coolant Temperature sensor is   │
                   │ reading low voltage. Your car might overheat.│
                   │ Recommended action: Inspect coolant level."  │
                   └──────────────────────────────────────────────┘
```

- **Severity Levels:**
  - 🟢 **INFO:** Non-critical advisory (e.g., minor EVAP leak P0442).
  - 🟡 **WARNING:** Service recommended within 7 days (e.g., Oxygen sensor circuit P0130).
  - 🔴 **CRITICAL:** Stop driving immediately (e.g., Misfire damaging catalytic converter P0300, severe overheating).

---

## 15. Security, Encryption & Regulatory Compliance

1. **Hardware / Vehicle Isolation:**
   - Transceiver firmware enforces hardware read-only constraints; CAN transmission TX pins can be physically pulled to ground during normal operation to prevent bus injection attacks.
2. **Wireless & Network Security:**
   - BLE bonding uses numeric comparison pairing (LE Secure Connections, AES-128).
   - Mobile-to-Cloud uses HTTPS (TLS 1.3) with Certificate Pinning.
3. **Data Protection & Privacy Compliance:**
   - Adheres to India's **Digital Personal Data Protection Act (DPDPA 2023)**.
   - GPS and location data are stored only on-device unless explicitly opted-in for cloud fleet analytics.
   - User account deletion triggers full erasure of telemetry history across cloud databases.

---

## 16. Offline Behavior & Graceful Degradation

Mobile internet coverage across highways and rural regions in India is frequently intermittent. Guardian maintains fail-safe operation:

| Component | Online State (4G/5G) | Offline State (No Connectivity) |
|---|---|---|
| **Vehicle Telemetry** | Streamed live to cloud | Buffered locally in phone SQLite storage (up to 12 hours) |
| **Emergency Alerts** | Audio voice + Push notification | Local on-device buzzer / Guardian speaker chime + local audio alert |
| **AI Conversation** | Full LLM cloud reasoning | Pre-recorded local voice templates (e.g., *"Internet unavailable. Speed is 70 km/h, temperature is normal."*) |
| **Trip Storage** | Synced in real time | Stored in offline cache; automatically backfills upon reconnection |

---

## 17. Hardware Cost Target (BOM)

To achieve the retail price point of **₹8,000 – ₹10,000 INR** ($95 – $120 USD), the target manufacturing Bill of Materials (BOM) for the V1 hardware module is:

| Component | Subsystem | Target Unit Cost (USD) | Target Unit Cost (INR) |
|---|---|---|---|
| ESP32-S3-WROOM-1 | Microcontroller / RF | $2.80 | ₹235 |
| STN1110 / ELM327 Circuit | OBD-II Transceiver + passive components | $3.50 | ₹295 |
| INMP441 Dual MEMS Mics | Audio input / I2S | $1.20 | ₹100 |
| MAX98357A + 3W Speaker | Audio output amplifier & driver | $1.80 | ₹150 |
| LM2596 Power / Protection | 12V to 5V DC step-down & transient diode| $1.60 | ₹135 |
| Custom OBD Enclosure | Injection-molded ABS housing | $2.20 | ₹185 |
| PCB, Connectors & Assembly | 4-layer FR4 PCB & SMT assembly | $3.40 | ₹285 |
| Packaging & User Manual | Eco-friendly box & quick-start guide | $1.50 | ₹125 |
| **Total Hardware BOM Cost**| — | **$18.00** | **₹1,510** |

*With 4.5x–5x retail markup covering cloud infrastructure, software development, distribution margins, and GST (18%), an ₹1,510 BOM translates directly into the target ₹7,999 – ₹8,999 introductory retail price.*

---

## 18. Performance & Latency Budgets

| Metric | Target Threshold | Maximum Acceptable Limit |
|---|---|---|
| **OBD-II Query to BLE Broadcast** | 100 ms | 250 ms |
| **Wake-Word Detection to Mic Active** | 150 ms | 300 ms |
| **User Speech End to STT Result** | 350 ms | 600 ms |
| **LLM Reasoning First-Token Latency** | 400 ms | 800 ms |
| **TTS Audio Playback Start** | 200 ms | 400 ms |
| **Total Voice Loop Turnaround** | **< 1.20 s** | **< 2.00 s** |
| **Standby Current Draw (Engine Off)** | < 3 mA (prevents battery drain) | < 8 mA |

---

## 19. Testing / Acceptance Criteria

### Physical Prototype V1 "Definition of Done"
A physical prototype test passes only when the following end-to-end loop executes reliably on a physical vehicle:

```
1. Vehicle ignition turns ON.
2. Guardian powers up in < 2 seconds, connects via BLE to the phone app.
3. Guardian identifies driver and speaks: "Welcome back, [Driver Name]."
4. Driver asks: "How is the car doing?"
5. Guardian reads coolant temperature, battery voltage, and RPM from OBD-II.
6. Guardian responds within 1.5 seconds: "Your coolant temperature is 90 degrees and battery voltage is 14.1 volts. Everything looks healthy."
7. Driver asks: "Why is my RPM high?"
8. Guardian correlates current gear/speed with RPM and gives an accurate explanation.
9. Vehicle ignition turns OFF ➔ Guardian stores trip log, wipes ephemeral conversation RAM, and enters deep sleep (<3mA).
```

### Initial Vehicle Compatibility Matrix
| Brand | Model | Year | OBD-II Link | RPM | Speed | Coolant | Battery Voltage | DTC Scan | Result |
|---|---|---|---|---|---|---|---|---|---|
| Hyundai | i20 / Creta | 2018–2023 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **PASS** |
| Maruti Suzuki| Swift / Baleno | 2017–2023 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **PASS** |
| Tata | Nexon / Altroz | 2019–2023 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **PASS** |
| Kia | Seltos / Sonet | 2020–2024 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **PASS** |
| Honda | City | 2015–2022 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **PASS** |

---

*This document serves as the frozen baseline specification for Guardian V1. All subsequent mobile, backend, firmware, and prototype hardware implementations must strictly adhere to the contracts defined herein.*
