package com.velora.backend.ai.orchestrator;

import java.util.List;

public class IntentPlan {
    private final String intentCategory; // e.g., VEHICLE_HEALTH, DIAGNOSTICS, TRIP, FUEL, EXTERNAL, GENERAL
    private final List<String> requiredTools; // e.g., ["get_telemetry", "get_diagnostics"]

    public IntentPlan(String intentCategory, List<String> requiredTools) {
        this.intentCategory = intentCategory;
        this.requiredTools = requiredTools;
    }

    public String getIntentCategory() {
        return intentCategory;
    }

    public List<String> getRequiredTools() {
        return requiredTools;
    }
}
