#include "packet_encoder.h"
#include <stddef.h>

uint16_t calculate_fletcher16(const uint8_t *data, size_t len) {
    uint16_t sum1 = 0;
    uint16_t sum2 = 0;

    for (size_t i = 0; i < len; ++i) {
        sum1 = (sum1 + data[i]) % 255;
        sum2 = (sum2 + sum1) % 255;
    }

    return (sum2 << 8) | sum1;
}

void encode_telemetry_packet(GuardianTelemetryPacket *packet,
                             uint16_t rpm,
                             uint8_t speed_kmh,
                             int16_t coolant_temp_c,
                             uint8_t throttle_pct,
                             uint8_t fuel_level_pct,
                             uint16_t battery_mv,
                             EngineState engine_state,
                             uint8_t dtc_count,
                             uint16_t sequence_id) {
    if (!packet) return;

    packet->magic_byte = 0x47; // 'G'
    packet->rpm = rpm;
    packet->speed_kmh = speed_kmh;
    packet->coolant_temp_c = coolant_temp_c;
    packet->throttle_pct = throttle_pct;
    packet->fuel_level_pct = fuel_level_pct;
    packet->battery_mv = battery_mv;
    packet->engine_state = (uint8_t)engine_state;
    packet->dtc_count = dtc_count;
    packet->sequence_id = sequence_id;
    packet->checksum = 0; // Clear before calculation

    // Calculate checksum over all bytes except the checksum itself (first 14 bytes)
    packet->checksum = calculate_fletcher16((const uint8_t *)packet, sizeof(GuardianTelemetryPacket) - sizeof(uint16_t));
}

bool validate_telemetry_packet(const GuardianTelemetryPacket *packet) {
    if (!packet || packet->magic_byte != 0x47) return false;

    uint16_t expected_checksum = calculate_fletcher16((const uint8_t *)packet, sizeof(GuardianTelemetryPacket) - sizeof(uint16_t));
    return (expected_checksum == packet->checksum);
}
