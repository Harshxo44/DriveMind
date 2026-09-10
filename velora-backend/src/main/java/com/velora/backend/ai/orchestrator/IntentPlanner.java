package com.velora.backend.ai.orchestrator;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class IntentPlanner {

    /**
     * Highly efficient deterministic heuristic intent router to map user natural language
     * into categorized domains and requested tooling.
     *
     * In an advanced AI orchestration pipeline, an LLM handles tool selection dynamically,
     * but this provides the resilient fallback routing for cost-free, high-speed execution.
     */
    public IntentPlan planIntentDeterministic(String userQuery) {
        String query = (userQuery == null) ? "" : userQuery.trim().toLowerCase();

        List<String> tools = new ArrayList<>();
        String category = "GENERAL";

        // Domain: Vehicle Health & Telemetry
        if (query.matches(".*(how.*car|doing|status|health|engine|oil|temp).*")) {
            category = "VEHICLE_HEALTH";
            tools.add("get_telemetry");
        }
        // Domain: Powertrain
        else if (query.matches(".*(rpm|rev|accelerat|speed|shift|power|torque).*")) {
            category = "POWERTRAIN";
            tools.add("get_telemetry");
            tools.add("get_automotive_knowledge");
        }
        // Domain: Fuel & Efficiency
        else if (query.matches(".*(fuel|range|mileage|efficiency|mpg|gas|petrol|diesel).*")) {
            category = "FUEL";
            tools.add("get_telemetry");
            tools.add("get_automotive_knowledge");
            tools.add("get_external_info"); // Optional location / fuel pump info
        }
        // Domain: Diagnostics / OBD-II
        else if (query.matches(".*(dtc|code|check engine|fault|warning light|breakdown|issue|problem).*")) {
            category = "DIAGNOSTICS";
            tools.add("get_telemetry");
            tools.add("get_diagnostics_info");
            tools.add("get_automotive_knowledge");
        }
        // Domain: Trip & User Context
        else if (query.matches(".*(trip|distance|time|how long|drive|journey|destination).*")) {
            category = "TRIP";
            tools.add("get_telemetry");
            tools.add("get_driver_and_trip");
        }
        // Domain: External World / Web Search
        else if (query.matches(".*(weather|traffic|navigate|where|news|price).*")) {
            category = "EXTERNAL";
            tools.add("get_external_info");
        }
        // General Chat / Persona
        else {
            category = "GENERAL";
            tools.add("get_telemetry"); // Minimal baseline awareness
        }

        return new IntentPlan(category, tools);
    }
}
