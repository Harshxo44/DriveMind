package com.velora.backend.ai.orchestrator;

import com.velora.backend.ai.dto.AiConversationRequest;
import com.velora.backend.ai.dto.AiConversationResponse;
import com.velora.backend.ai.dto.TelemetryContextDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class SafetyPolicy {

    /**
     * Zero-latency deterministic safety check.
     * Evaluates live telemetry for critical engine, electrical, or physical safety risks.
     */
    public static Optional<AiConversationResponse> evaluate(AiConversationRequest request) {
        TelemetryContextDto telemetry = request.telemetry();
        String driverName = (request.driver() != null && request.driver().name() != null && !request.driver().name().isBlank())
                ? request.driver().name() : "Driver";

        if (telemetry == null) {
            return Optional.empty();
        }

        // 1. Critical Engine Coolant Overheating (> 105°C)
        if (telemetry.coolantTempC() != null && telemetry.coolantTempC() > 105.0) {
            String spokenReply = String.format("Warning %s: Engine coolant is high at %.0f degrees Celsius. Please pull over safely.",
                    driverName, telemetry.coolantTempC());
            String displaySummary = "Critical Overheating Alert: Coolant temp exceeds threshold.";
            List<String> followUps = List.of("Check coolant sensor", "Find nearest service station");
            return Optional.of(new AiConversationResponse(spokenReply, displaySummary, "ENGINE_THERMAL", true, followUps, Instant.now()));
        }

        // 2. Critical Low Battery Voltage (< 11.8V)
        if (telemetry.batteryVoltage() != null && telemetry.batteryVoltage() < 11.8) {
            String spokenReply = String.format("Alert %s: Battery voltage dropped to %.1f volts. Alternator charging may be compromised.",
                    driverName, telemetry.batteryVoltage());
            String displaySummary = "Low Battery Voltage: Below 11.8V safe threshold.";
            List<String> followUps = List.of("Test battery alternator");
            return Optional.of(new AiConversationResponse(spokenReply, displaySummary, "ELECTRICAL", true, followUps, Instant.now()));
        }

        return Optional.empty();
    }
}
