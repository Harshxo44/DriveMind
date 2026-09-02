#include "engine_state_machine.h"

void engine_state_init(EngineStateMachine *sm) {
    if (!sm) return;
    sm->current_state = ENGINE_STATE_DISCONNECTED;
    sm->last_rpm_time_ms = 0;
    sm->engine_off_timestamp_ms = 0;
    sm->trigger_privacy_purge = false;
}

EngineState engine_state_update(EngineStateMachine *sm,
                                uint16_t rpm,
                                uint16_t battery_mv,
                                bool obd_connected,
                                uint32_t current_time_ms) {
    if (!obd_connected) {
        sm->current_state = ENGINE_STATE_DISCONNECTED;
        return sm->current_state;
    }

    bool engine_is_rotating = (rpm > 400);

    switch (sm->current_state) {
        case ENGINE_STATE_DISCONNECTED:
        case ENGINE_STATE_SLEEP:
            if (engine_is_rotating) {
                sm->current_state = ENGINE_STATE_RUNNING;
            } else if (obd_connected && battery_mv > 10000) {
                sm->current_state = ENGINE_STATE_IGNITION_ON;
            }
            break;

        case ENGINE_STATE_IGNITION_ON:
            if (engine_is_rotating) {
                sm->current_state = ENGINE_STATE_RUNNING;
            } else if (current_time_ms - sm->engine_off_timestamp_ms > 60000) {
                sm->current_state = ENGINE_STATE_SLEEP;
            }
            break;

        case ENGINE_STATE_RUNNING:
            if (!engine_is_rotating) {
                sm->current_state = ENGINE_STATE_ENGINE_OFF;
                sm->engine_off_timestamp_ms = current_time_ms;
                sm->trigger_privacy_purge = true; // DPDPA memory flush toggle
            }
            break;

        case ENGINE_STATE_ENGINE_OFF:
            if (engine_is_rotating) {
                sm->current_state = ENGINE_STATE_RUNNING;
                sm->trigger_privacy_purge = false;
            } else if (current_time_ms - sm->engine_off_timestamp_ms > 60000) {
                sm->current_state = ENGINE_STATE_SLEEP;
            }
            break;
    }

    return sm->current_state;
}
