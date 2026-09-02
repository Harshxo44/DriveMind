#pragma once
#include <stdint.h>
#include <stdbool.h>

#pragma pack(push, 1)
/**
 * Guardian V1 Binary Telemetry Packet (16 Bytes Packed)
 * Transmitted over BLE GATT Characteristic at 5 Hz.
 */
typedef struct {
    uint8_t  magic_byte;         // 0x47 ('G') - Frame sync
    uint16_t rpm;                // Engine RPM (0 - 8000)
    uint8_t  speed_kmh;          // Vehicle Speed (0 - 255 km/h)
    int16_t  coolant_temp_c;     // Coolant Temp (-40 to +215 °C)
    uint8_t  throttle_pct;       // Throttle Position (0 - 100 %)
    uint8_t  fuel_level_pct;     // Fuel Level (0 - 100 %)
    uint16_t battery_mv;         // 12V Battery Voltage (e.g. 14100 = 14.10V)
    uint8_t  engine_state;       // 0: OFF, 1: IGNITION_ON, 2: RUNNING, 3: FAULT
    uint8_t  dtc_count;          // Number of active Diagnostic Trouble Codes
    uint16_t sequence_id;        // Monotonically increasing packet ID
    uint16_t checksum;           // 16-bit CRC/Fletcher-16 checksum
} GuardianTelemetryPacket;
#pragma pack(pop)

// Engine State Enum
typedef enum {
    ENGINE_STATE_DISCONNECTED = 0,
    ENGINE_STATE_IGNITION_ON  = 1,
    ENGINE_STATE_RUNNING      = 2,
    ENGINE_STATE_ENGINE_OFF   = 3,
    ENGINE_STATE_SLEEP        = 4
} EngineState;
