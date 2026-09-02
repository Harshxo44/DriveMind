#pragma once
#include "telemetry_model.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Calculates Fletcher-16 checksum over a byte buffer.
 */
uint16_t calculate_fletcher16(const uint8_t *data, size_t len);

/**
 * Populates a GuardianTelemetryPacket and computes its checksum.
 */
void encode_telemetry_packet(GuardianTelemetryPacket *packet,
                             uint16_t rpm,
                             uint8_t speed_kmh,
                             int16_t coolant_temp_c,
                             uint8_t throttle_pct,
                             uint8_t fuel_level_pct,
                             uint16_t battery_mv,
                             EngineState engine_state,
                             uint8_t dtc_count,
                             uint16_t sequence_id);

/**
 * Validates magic byte and checksum of an incoming packet.
 */
bool validate_telemetry_packet(const GuardianTelemetryPacket *packet);

#ifdef __cplusplus
}
#endif
