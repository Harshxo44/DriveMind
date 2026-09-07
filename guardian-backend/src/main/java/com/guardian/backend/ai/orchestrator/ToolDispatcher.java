package com.guardian.backend.ai.orchestrator;

import com.guardian.backend.ai.dto.AiConversationRequest;
import com.guardian.backend.ai.dto.TelemetryContextDto;
import com.guardian.backend.diagnostics.DiagnosticTroubleCodeRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ToolDispatcher {

    private final DiagnosticTroubleCodeRepository dtcRepository;

    public ToolDispatcher(DiagnosticTroubleCodeRepository dtcRepository) {
        this.dtcRepository = dtcRepository;
    }

    /**
     * Dispatches the required tools based on the IntentPlan and gathers structured data for reasoning.
     * In a full micro-services orchestrator architecture, these would be separate tool execution plugins.
     */
    public Map<String, Object> executeTools(IntentPlan intentPlan, AiConversationRequest request) {
        Map<String, Object> gatheredContext = new HashMap<>();

        for (String toolName : intentPlan.getRequiredTools()) {
            switch (toolName) {
                case "get_telemetry":
                    gatheredContext.put("telemetry", executeGetTelemetry(request));
                    break;
                case "get_diagnostics_info":
                    gatheredContext.put("diagnostics", executeGetDiagnosticsInfo(request));
                    break;
                case "get_driver_and_trip":
                    gatheredContext.put("trip", executeGetDriverAndTrip(request));
                    break;
                case "get_automotive_knowledge":
                    gatheredContext.put("knowledge", "General automotive rule: High RPM > 3000 at low speeds wastes fuel. DTC P0300 means random cylinder misfire. Keep battery above 12.0V when engine is off.");
                    break;
                case "get_external_info":
                    gatheredContext.put("external", "Weather is currently clear. No active traffic delays along the route. Nearest service center is 5km away.");
                    break;
            }
        }
        return gatheredContext;
    }

    private Object executeGetTelemetry(AiConversationRequest request) {
        if (request.telemetry() == null) {
            return Map.of("status", "unavailable");
        }
        return request.telemetry();
    }

    private Object executeGetDiagnosticsInfo(AiConversationRequest request) {
        TelemetryContextDto t = request.telemetry();
        if (t != null && t.dtcActive() != null && !t.dtcActive().isEmpty()) {
            Map<String, String> dtcExplanations = new HashMap<>();
            for (String code : t.dtcActive()) {
                String desc = dtcRepository.findByCodeIgnoreCase(code)
                        .map(dtc -> dtc.getDescription())
                        .orElse("Unknown powertrain sensor malfunction");
                dtcExplanations.put(code, desc);
            }
            return dtcExplanations;
        }
        return Map.of("status", "All systems clear, 0 active faults.");
    }

    private Object executeGetDriverAndTrip(AiConversationRequest request) {
        Map<String, Object> tripContext = new HashMap<>();
        tripContext.put("driver", request.driver() != null ? request.driver() : "Unknown");
        tripContext.put("vehicle", request.vehicle() != null ? request.vehicle() : "Unknown Vehicle");
        tripContext.put("trip", request.trip() != null ? request.trip() : "No trip active");
        return tripContext;
    }
}
