#pragma once
#include "../telemetry/telemetry_model.h"
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

// Guardian V1 128-bit BLE UUID Definitions
#define GUARDIAN_SERVICE_UUID           "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define GUARDIAN_CHAR_TELEMETRY_UUID    "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define GUARDIAN_CHAR_COMMAND_UUID      "8ec94abe-f16b-48ed-a2b2-4d1a084c794e"

typedef struct {
    bool is_connected;
    uint8_t client_count;
    uint32_t packets_sent;
} BleServerState;

/**
 * Initializes ESP32 BLE GATT Server and starts advertising as "Guardian-OBD-XXXX".
 */
void ble_server_init(const char *device_name_suffix);

/**
 * Transmits a 16-byte packed binary telemetry packet to connected BLE subscribers via GATT Notification.
 */
bool ble_server_notify_telemetry(const GuardianTelemetryPacket *packet);

/**
 * Get current BLE connection status and statistics.
 */
void ble_server_get_state(BleServerState *out_state);

#ifdef __cplusplus
}
#endif
