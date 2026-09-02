#pragma once
#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Parsed Mode 01 PID values.
 */
typedef struct {
    bool has_rpm;
    uint16_t rpm;

    bool has_speed;
    uint8_t speed_kmh;

    bool has_coolant;
    int16_t coolant_temp_c;

    bool has_throttle;
    uint8_t throttle_pct;

    bool has_fuel;
    uint8_t fuel_level_pct;
} ParsedObdData;

/**
 * Parses raw hex string response from ELM327/STN1110 (e.g. "41 0C 0B D4").
 * Populates the out_data struct with the extracted value.
 * Returns true if properly parsed as a Mode 01 response for a recognized PID.
 */
bool parse_obd_response(const char *hex_str, ParsedObdData *out_data);

#ifdef __cplusplus
}
#endif
