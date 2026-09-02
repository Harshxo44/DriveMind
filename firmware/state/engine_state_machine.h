#pragma once
#include "../telemetry/telemetry_model.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    EngineState current_state;
    uint32_t    last_rpm_time_ms;
    uint32_t    engine_off_timestamp_ms;
    bool        trigger_privacy_purge;
} EngineStateMachine;

void engine_state_init(EngineStateMachine *sm);

/**
 * Process a periodic sensor telemetry update and evaluate state machine transitions.
 */
EngineState engine_state_update(EngineStateMachine *sm,
                                uint16_t rpm,
                                uint16_t battery_mv,
                                bool obd_connected,
                                uint32_t current_time_ms);

#ifdef __cplusplus
}
#endif
