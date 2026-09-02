#include "pid_discovery.h"
#include <string.h>

void pid_discovery_init(PidDiscoveryTable *table) {
    if (!table) return;
    memset(table, 0, sizeof(PidDiscoveryTable));
}

bool pid_discovery_parse_bitmask(PidDiscoveryTable *table, uint8_t base_pid, uint32_t bitmask) {
    if (!table) return false;

    if (base_pid != 0x00 && base_pid != 0x20 && base_pid != 0x40) {
        return false;
    }

    if (base_pid == 0x00) table->pid_00_discovered = true;
    if (base_pid == 0x20) table->pid_20_discovered = true;
    if (base_pid == 0x40) table->pid_40_discovered = true;

    // Bit 31 corresponds to base_pid + 1, Bit 0 corresponds to base_pid + 32
    for (int i = 0; i < 32; i++) {
        uint8_t current_pid = base_pid + (32 - i);
        if (current_pid <= 0x60) {
            table->supported_pids[current_pid] = ((bitmask & (1U << i)) != 0) ? 1 : 0;
        }
    }

    return true;
}

bool is_pid_supported(const PidDiscoveryTable *table, uint8_t pid) {
    if (!table || pid > 0x60) return false;
    return (table->supported_pids[pid] == 1);
}
