#pragma once
#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    bool pid_00_discovered;
    bool pid_20_discovered;
    bool pid_40_discovered;

    // Bitmap of supported PIDs (1-indexed for PID 0x01 to 0x60)
    uint8_t supported_pids[0x60 + 1];
} PidDiscoveryTable;

/**
 * Reset PID discovery table.
 */
void pid_discovery_init(PidDiscoveryTable *table);

/**
 * Ingest bitmask returned for Mode 01 PID 00, 20, or 40.
 * e.g., for PID 00: "41 00 BE 3E A8 13"
 */
bool pid_discovery_parse_bitmask(PidDiscoveryTable *table, uint8_t base_pid, uint32_t bitmask);

/**
 * Check if a specific PID is supported by the connected ECU.
 */
bool is_pid_supported(const PidDiscoveryTable *table, uint8_t pid);

#ifdef __cplusplus
}
#endif
