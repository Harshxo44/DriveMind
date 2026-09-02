// Phase 5 Bench ECU Simulator Harness / OBD-II UART Response test suite
#include "obd/pid_parser.h"
#include "telemetry/packet_encoder.h"
#include "state/engine_state_machine.h"
#include "obd/pid_discovery.h"
#include <stdio.h>
#include <string.h>

void run_pid_parser_tests() {
    printf("--- Running PID Parser Tests ---\n");
    ParsedObdData data = {0};

    // Test RPM: 41 0C 0B D4 -> 757 RPM
    if (parse_obd_response("41 0C 0B D4", &data) && data.has_rpm && data.rpm == 757)
        printf("✅ RPM Parsing Passed (757)\n");
    else
        printf("❌ RPM Parsing Failed\n");

    // Test Speed: 41 0D 2A -> 42 km/h
    memset(&data, 0, sizeof(data));
    if (parse_obd_response("41 0D 2A", &data) && data.has_speed && data.speed_kmh == 42)
        printf("✅ Speed Parsing Passed (42)\n");
    else
        printf("❌ Speed Parsing Failed\n");

    // Test Coolant: 41 05 7B -> 123-40 = 83°C
    memset(&data, 0, sizeof(data));
    if (parse_obd_response("41 05 7B", &data) && data.has_coolant && data.coolant_temp_c == 83)
        printf("✅ Coolant Parsing Passed (83°C)\n");
    else
        printf("❌ Coolant Parsing Failed\n");

    // Test Fuel: 41 2F 4B -> 75*100/255 = 29%
    memset(&data, 0, sizeof(data));
    if (parse_obd_response("41 2F 4B", &data) && data.has_fuel && data.fuel_level_pct == 29)
        printf("✅ Fuel Parsing Passed (29%%)\n");
    else
        printf("❌ Fuel Parsing Failed\n");
}

void run_packet_encoder_tests() {
    printf("\n--- Running Packet Encoder Tests ---\n");
    GuardianTelemetryPacket pkt;
    encode_telemetry_packet(&pkt, 2400, 68, 91, 20, 74, 14100, ENGINE_STATE_RUNNING, 0, 1001);

    if (pkt.magic_byte == 0x47 && pkt.rpm == 2400 && pkt.engine_state == 2) {
        printf("✅ Packet Mapping Passed\n");
    } else {
        printf("❌ Packet Mapping Failed\n");
    }

    if (validate_telemetry_packet(&pkt)) {
        printf("✅ Packet Checksum Passed\n");
    } else {
        printf("❌ Packet Checksum check failed\n");
    }
}

int main() {
    printf("==========================================\n");
    printf("GUARDIAN V1 - BENCH FIRMWARE VALIDATION\n");
    printf("==========================================\n\n");
    run_pid_parser_tests();
    run_packet_encoder_tests();
    return 0;
}
