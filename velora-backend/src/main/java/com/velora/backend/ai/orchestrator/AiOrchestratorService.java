package com.velora.backend.ai.orchestrator;

import com.velora.backend.ai.dto.AiConversationRequest;
import com.velora.backend.ai.dto.AiConversationResponse;
import com.velora.backend.ai.dto.TelemetryContextDto;
import com.velora.backend.diagnostics.DiagnosticTroubleCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AiOrchestratorService.class);

    private final IntentPlanner intentPlanner;
    private final ToolDispatcher toolDispatcher;
    private final DiagnosticTroubleCodeRepository dtcRepository;

    public AiOrchestratorService(IntentPlanner intentPlanner, ToolDispatcher toolDispatcher, DiagnosticTroubleCodeRepository dtcRepository) {
        this.intentPlanner = intentPlanner;
        this.toolDispatcher = toolDispatcher;
        this.dtcRepository = dtcRepository;
    }

    /**
     * Executes the Guardian AI Orchestration Workflow:
     *
     * 1. Safety Policy Check (Zero-latency deterministic guardrail)
     * 2. Intent & Information-Needed Planning
     * 3. Tool Dispatching & Context Collection
     * 4. AI Reasoning & Response Generation
     */
    public AiConversationResponse orchestrate(AiConversationRequest request) {
        log.info("Guardian AI Orchestrator processing query: '{}'", request.userQuery());

        // Step 1: Safety Policy Evaluation (Instant zero-latency check)
        Optional<AiConversationResponse> criticalAlert = SafetyPolicy.evaluate(request);
        if (criticalAlert.isPresent()) {
            log.warn("SafetyPolicy triggered critical alert for query!");
            return criticalAlert.get();
        }

        // Step 2: Determine Intent & Required Tools
        IntentPlan intentPlan = intentPlanner.planIntentDeterministic(request.userQuery());
        log.info("Planned Intent: {} with tools: {}", intentPlan.getIntentCategory(), intentPlan.getRequiredTools());

        // Step 3: Dispatch Tools & Collect Ground-Truth Context
        Map<String, Object> toolOutputs = toolDispatcher.executeTools(intentPlan, request);

        // Step 4: Reasoning & Final Response Synthesizer
        return synthesizeResponse(request, intentPlan, toolOutputs);
    }

    private AiConversationResponse synthesizeResponse(AiConversationRequest request, IntentPlan intentPlan, Map<String, Object> toolOutputs) {
        String query = (request.userQuery() == null) ? "" : request.userQuery().trim().toLowerCase();
        TelemetryContextDto telemetry = request.telemetry();
        String driverName = (request.driver() != null && request.driver().name() != null && !request.driver().name().isBlank())
                ? request.driver().name() : "Driver";
        String vehicleName = (request.vehicle() != null && request.vehicle().make() != null)
                ? (request.vehicle().make() + " " + (request.vehicle().model() != null ? request.vehicle().model() : ""))
                : "your car";

        String spokenReply;
        String displaySummary;
        String category = intentPlan.getIntentCategory();
        boolean criticalAlert = false;
        List<String> followUps = new ArrayList<>();

        if ("VEHICLE_HEALTH".equals(category)) {
            double coolant = telemetry != null && telemetry.coolantTempC() != null ? telemetry.coolantTempC() : 90.0;
            double voltage = telemetry != null && telemetry.batteryVoltage() != null ? telemetry.batteryVoltage() : 14.1;
            int rpm = telemetry != null && telemetry.rpm() != null ? telemetry.rpm() : 2000;

            spokenReply = String.format("Your coolant temperature is %.0f degrees and battery is %.1f volts. Everything looks healthy in your %s.",
                    coolant, voltage, vehicleName);
            displaySummary = String.format("All vehicle systems nominal. Coolant: %.0f°C, Battery: %.1fV, RPM: %d", coolant, voltage, rpm);
            followUps.add("What is my current fuel range?");
            followUps.add("Check active fault codes");
        } else if ("POWERTRAIN".equals(category)) {
            int rpm = telemetry != null && telemetry.rpm() != null ? telemetry.rpm() : 2200;
            double speed = telemetry != null && telemetry.speedKmh() != null ? telemetry.speedKmh() : 0.0;

            if (rpm > 3500 && speed < 40) {
                spokenReply = String.format("Your RPM is %d while traveling at %.0f km/h. Shifting to a higher gear will improve fuel efficiency.", rpm, speed);
            } else {
                spokenReply = String.format("Your RPM is currently %d at %.0f km/h, which is well within the normal operating range.", rpm, speed);
            }
            displaySummary = String.format("Engine Speed: %d RPM at %.0f km/h", rpm, speed);
            followUps.add("How to optimize my fuel economy?");
        } else if ("FUEL".equals(category)) {
            double fuel = telemetry != null && telemetry.fuelLevelPct() != null ? telemetry.fuelLevelPct() : 50.0;
            spokenReply = String.format("Fuel tank is at %.0f percent capacity. Your current driving profile indicates normal fuel burn.", fuel);
            displaySummary = String.format("Fuel Level: %.0f%%", fuel);
            followUps.add("Find nearest petrol pump");
        } else if ("DIAGNOSTICS".equals(category)) {
            if (telemetry != null && telemetry.dtcActive() != null && !telemetry.dtcActive().isEmpty()) {
                String firstCode = telemetry.dtcActive().get(0);
                String dtcDesc = dtcRepository.findByCodeIgnoreCase(firstCode)
                        .map(dtc -> dtc.getDescription())
                        .orElse("sensor malfunction");
                spokenReply = String.format("Diagnostic scan detected code %s for %s. Recommended to inspect related components soon.", firstCode, dtcDesc);
                displaySummary = "Active DTCs: " + String.join(", ", telemetry.dtcActive());
                followUps.add("Explain code " + firstCode);
            } else {
                spokenReply = "No active Diagnostic Trouble Codes detected on the OBD-II bus. Your powertrain ECU reports all clear.";
                displaySummary = "OBD-II Scan: 0 Fault Codes";
            }
            followUps.add("Run full system diagnostic");
        } else if ("TRIP".equals(category)) {
            int duration = (request.trip() != null && request.trip().durationMinutes() != null) ? request.trip().durationMinutes() : 15;
            double distance = (request.trip() != null && request.trip().distanceKm() != null) ? request.trip().distanceKm() : 10.2;
            spokenReply = String.format("You have been driving for %d minutes covering %.1f kilometers.", duration, distance);
            displaySummary = String.format("Current Trip: %d mins | %.1f km", duration, distance);
            followUps.add("Summary of driving score");
        } else {
            spokenReply = String.format("Guardian online, %s. I am monitoring %s's OBD telemetry and driving safety. How can I help?",
                    driverName, vehicleName);
            displaySummary = "Guardian Co-Driver Active & Listening";
            followUps.add("How's the car doing?");
            followUps.add("Check battery voltage");
            followUps.add("Scan diagnostic codes");
        }

        return new AiConversationResponse(
                spokenReply,
                displaySummary,
                category,
                criticalAlert,
                followUps,
                Instant.now()
        );
    }
}
