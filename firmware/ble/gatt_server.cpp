#include "gatt_server.h"
#include <stdio.h>
#include <string.h>

static BleServerState g_ble_state = { false, 0, 0 };
static char g_device_name[32] = "Guardian-OBD-0001";

void ble_server_init(const char *device_name_suffix) {
    if (device_name_suffix && strlen(device_name_suffix) > 0) {
        snprintf(g_device_name, sizeof(g_device_name), "Guardian-OBD-%s", device_name_suffix);
    }
    g_ble_state.is_connected = false;
    g_ble_state.client_count = 0;
    g_ble_state.packets_sent = 0;

    printf("[BLE] Initialized GATT Server with Device Name: %s\n", g_device_name);
    printf("[BLE] Service UUID: %s\n", GUARDIAN_SERVICE_UUID);
    printf("[BLE] Telemetry NOTIFY Characteristic: %s\n", GUARDIAN_CHAR_TELEMETRY_UUID);
    printf("[BLE] Advertising Started...\n");
}

bool ble_server_notify_telemetry(const GuardianTelemetryPacket *packet) {
    if (!packet) return false;

    // In hardware runtime, this dispatches via esp_ble_gatts_send_indicate/notify
    g_ble_state.packets_sent++;
    return true;
}

void ble_server_get_state(BleServerState *out_state) {
    if (!out_state) return;
    *out_state = g_ble_state;
}
