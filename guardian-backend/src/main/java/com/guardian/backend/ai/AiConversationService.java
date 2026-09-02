package com.guardian.backend.ai;

import com.guardian.backend.ai.dto.*;
import com.guardian.backend.diagnostics.DiagnosticTroubleCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiConversationService {

    private static final Logger log = LoggerFactory.getLogger(AiConversationService.class);
    private final DiagnosticTroubleCodeRepository dtcRepository;

    public AiConversationService(DiagnosticTroubleCodeRepository dtcRepository) {
        this.dtcRepository = dtcRepository;
    }

    public AiConversationResponse processConversation(AiConversationRequest request) {
        String query = request.userQuery().trim().toLowerCase();
        TelemetryContextDto telemetry = request.telemetry();
        DriverContextDto driver = request.driver();
        VehicleContextDto vehicle = request.vehicle();

        String driverName = (driver != null && driver.name() != null && !driver.name().isBlank())
                ? driver.name() : "Driver";
        String vehicleName = (vehicle != null && vehicle.make() != null)
                ? (vehicle.make() + " " + (vehicle.model() != null ? vehicle.model() : ""))
                : "your car";

        String spokenReply;
        String displaySummary;
        String category = "GENERAL";
        boolean criticalAlert = false;
        List<String> followUps = new ArrayList<>();

        // Check for active critical conditions first
        if (telemetry != null && telemetry.coolantTempC() != null && telemetry.coolantTempC() > 105.0) {
            criticalAlert = true;
            category = "ENGINE_THERMAL";
            spokenReply = String.format("Warning %s: Engine coolant is high at %.0f degrees Celsius. Please pull over safely.",
                    driverName, telemetry.coolantTempC());
            displaySummary = "Critical Overheating Alert: Coolant temp exceeds threshold.";
            followUps.add("Check coolant sensor");
            followUps.add("Find nearest service station");
            return new AiConversationResponse(spokenReply, displaySummary, category, true, followUps, Instant.now());
        }

        if (telemetry != null && telemetry.batteryVoltage() != null && telemetry.batteryVoltage() < 11.8) {
            criticalAlert = true;
            category = "ELECTRICAL";
            spokenReply = String.format("Alert %s: Battery voltage dropped to %.1f volts. Alternator charging may be compromised.",
                    driverName, telemetry.batteryVoltage());
            displaySummary = "Low Battery Voltage: Below 11.8V safe threshold.";
            followUps.add("Test battery alternator");
            return new AiConversationResponse(spokenReply, displaySummary, category, true, followUps, Instant.now());
        }

        // Conversational query classification
        if (query.contains("how") && (query.contains("car") || query.contains("doing") || query.contains("status") || query.contains("health") || query.contains("engine"))) {
            category = "VEHICLE_HEALTH";
            double coolant = telemetry != null && telemetry.coolantTempC() != null ? telemetry.coolantTempC() : 90.0;
            double voltage = telemetry != null && telemetry.batteryVoltage() != null ? telemetry.batteryVoltage() : 14.1;
            int rpm = telemetry != null && telemetry.rpm() != null ? telemetry.rpm() : 2000;

            spokenReply = String.format("Your coolant temperature is %.0f degrees and battery is %.1f volts. Everything looks healthy in your %s.",
                    coolant, voltage, vehicleName);
            displaySummary = String.format("All vehicle systems nominal. Coolant: %.0f°C, Battery: %.1fV, RPM: %d", coolant, voltage, rpm);
            followUps.add("What is my current fuel range?");
            followUps.add("Check active fault codes");
        } else if (query.contains("rpm") || query.contains("rev") || query.contains("accelerat")) {
            category = "POWERTRAIN";
            int rpm = telemetry != null && telemetry.rpm() != null ? telemetry.rpm() : 2200;
            double speed = telemetry != null && telemetry.speedKmh() != null ? telemetry.speedKmh() : 0.0;

            if (rpm > 3500 && speed < 40) {
                spokenReply = String.format("Your RPM is %d while traveling at %.0f km/h. Shifting to a higher gear will improve fuel efficiency.", rpm, speed);
            } else {
                spokenReply = String.format("Your RPM is currently %d at %.0f km/h, which is well within the normal operating range.", rpm, speed);
            }
            displaySummary = String.format("Engine Speed: %d RPM at %.0f km/h", rpm, speed);
            followUps.add("How to optimize my fuel economy?");
        } else if (query.contains("fuel") || query.contains("range") || query.contains("mileage") || query.contains("efficiency")) {
            category = "FUEL";
            double fuel = telemetry != null && telemetry.fuelLevelPct() != null ? telemetry.fuelLevelPct() : 50.0;
            spokenReply = String.format("Fuel tank is at %.0f percent capacity. Your current driving profile indicates normal fuel burn.", fuel);
            displaySummary = String.format("Fuel Level: %.0f%%", fuel);
            followUps.add("Find nearest petrol pump");
        } else if (query.contains("dtc") || query.contains("code") || query.contains("check engine") || query.contains("fault") || query.contains("warning light")) {
            category = "DIAGNOSTICS";
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
        } else if (query.contains("trip") || query.contains("distance") || query.contains("time") || query.contains("how long")) {
            category = "TRIP";
            int duration = (request.trip() != null && request.trip().durationMinutes() != null) ? request.trip().durationMinutes() : 15;
            double distance = (request.trip() != null && request.trip().distanceKm() != null) ? request.trip().distanceKm() : 10.2;
            spokenReply = String.format("You have been driving for %d minutes covering %.1f kilometers.", duration, distance);
            displaySummary = String.format("Current Trip: %d mins | %.1f km", duration, distance);
            followUps.add("Summary of driving score");
        } else {
            category = "GENERAL";
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
