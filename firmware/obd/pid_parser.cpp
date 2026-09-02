#include "pid_parser.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

static int hex_char_to_int(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'A' && c <= 'F') return c - 'A' + 10;
    if (c >= 'a' && c <= 'f') return c - 'a' + 10;
    return -1;
}

static int parse_hex_byte(const char *str) {
    int h = hex_char_to_int(str[0]);
    int l = hex_char_to_int(str[1]);
    if (h < 0 || l < 0) return -1;
    return (h << 4) | l;
}

bool parse_obd_response(const char *hex_str, ParsedObdData *out_data) {
    if (!hex_str || !out_data) return false;

    // ELM327 responses strip spaces or keep them based on config (AT S1/0).
    // Usually responses look like "41 0C 0B D4" or "410C0BD4".
    // We'll normalize by skipping spaces.
    char buffer[32] = {0};
    int b_idx = 0;
    for (int i = 0; hex_str[i] != '\0' && b_idx < 30; i++) {
        if (hex_str[i] != ' ' && hex_str[i] != '\r' && hex_str[i] != '\n') {
            buffer[b_idx++] = hex_str[i];
        }
    }
    buffer[b_idx] = '\0';

    if (b_idx < 4) return false; // Needs at least "41" and "PID"

    int mode = parse_hex_byte(&buffer[0]);
    if (mode != 0x41) return false; // Not a Mode 01 response

    int pid = parse_hex_byte(&buffer[2]);

    switch (pid) {
        case 0x0C: // Engine RPM (2 bytes)
            if (b_idx >= 8) {
                int a = parse_hex_byte(&buffer[4]);
                int b = parse_hex_byte(&buffer[6]);
                if (a >= 0 && b >= 0) {
                    out_data->rpm = ((a * 256) + b) / 4;
                    out_data->has_rpm = true;
                    return true;
                }
            }
            break;

        case 0x0D: // Vehicle Speed (1 byte)
            if (b_idx >= 6) {
                int a = parse_hex_byte(&buffer[4]);
                if (a >= 0) {
                    out_data->speed_kmh = a;
                    out_data->has_speed = true;
                    return true;
                }
            }
            break;

        case 0x05: // Coolant Temp (1 byte)
            if (b_idx >= 6) {
                int a = parse_hex_byte(&buffer[4]);
                if (a >= 0) {
                    out_data->coolant_temp_c = a - 40;
                    out_data->has_coolant = true;
                    return true;
                }
            }
            break;

        case 0x11: // Throttle Position (1 byte)
            if (b_idx >= 6) {
                int a = parse_hex_byte(&buffer[4]);
                if (a >= 0) {
                    out_data->throttle_pct = (uint8_t)((a * 100) / 255);
                    out_data->has_throttle = true;
                    return true;
                }
            }
            break;

        case 0x2F: // Fuel Level Input (1 byte)
            if (b_idx >= 6) {
                int a = parse_hex_byte(&buffer[4]);
                if (a >= 0) {
                    out_data->fuel_level_pct = (uint8_t)((a * 100) / 255);
                    out_data->has_fuel = true;
                    return true;
                }
            }
            break;
    }

    return false;
}
