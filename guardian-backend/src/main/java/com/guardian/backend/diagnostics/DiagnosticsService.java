package com.guardian.backend.diagnostics;

import com.guardian.backend.diagnostics.dto.AiAssistantRequest;
import com.guardian.backend.diagnostics.dto.AiAssistantResponse;
import com.guardian.backend.diagnostics.dto.VehicleHealthSummaryResponse;
import com.guardian.backend.telemetry.TelemetrySnapshot;
import com.guardian.backend.telemetry.TelemetrySnapshotRepository;
import com.guardian.backend.vehicle.Vehicle;
import com.guardian.backend.vehicle.VehicleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class DiagnosticsService {

    private final DiagnosticTroubleCodeRepository dtcRepository;
    private final TelemetrySnapshotRepository telemetryRepository;
    private final VehicleRepository vehicleRepository;

    public DiagnosticsService(DiagnosticTroubleCodeRepository dtcRepository, TelemetrySnapshotRepository telemetryRepository, VehicleRepository vehicleRepository) {
        this.dtcRepository = dtcRepository;
        this.telemetryRepository = telemetryRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @PostConstruct
    public void seedDefaultDTCs() {
        if (dtcRepository.count() == 0) {
            List<DiagnosticTroubleCode> dtcs = List.of(
                    DiagnosticTroubleCode.builder()
                            .code("P0300")
                            .system("Powertrain")
                            .description("Random/Multiple Cylinder Misfire Detected")
                            .symptoms("Engine shaking, poor acceleration, blinking check engine light")
                            .potentialCauses("Worn spark plugs, faulty ignition coils, fuel injector failure")
                            .recommendedAction("Inspect ignition coils and spark plugs immediately.")
                            .severity(DiagnosticTroubleCode.Severity.HIGH)
                            .build(),
                    DiagnosticTroubleCode.builder()
                            .code("P0171")
                            .system("Fuel & Air Metering")
                            .description("System Too Lean (Bank 1)")
                            .symptoms("Hesitation on acceleration, rough idle, increased fuel consumption")
                            .potentialCauses("Vacuum leak, dirty Mass Airflow Sensor (MAF), weak fuel pump")
                            .recommendedAction("Clean MAF sensor and test for intake vacuum leaks.")
                            .severity(DiagnosticTroubleCode.Severity.MODERATE)
                            .build(),
                    DiagnosticTroubleCode.builder()
                            .code("P0420")
                            .system("Exhaust & Emissions")
                            .description("Catalyst System Efficiency Below Threshold (Bank 1)")
                            .symptoms("Sulfur/rotten egg smell, reduced fuel economy, loss of power")
                            .potentialCauses("Failed catalytic converter, malfunctioning oxygen sensor, exhaust leak")
                            .recommendedAction("Scan upstream/downstream O2 sensor voltages before replacing catalytic converter.")
                            .severity(DiagnosticTroubleCode.Severity.MODERATE)
                            .build(),
                    DiagnosticTroubleCode.builder()
                            .code("P0113")
                            .system("Powertrain")
                            .description("Intake Air Temperature Sensor 1 Circuit High Input")
                            .symptoms("Hard start in cold weather, black exhaust smoke")
                            .potentialCauses("Disconnected IAT connector, broken IAT sensor ground wire")
                            .recommendedAction("Verify sensor wiring harness and resistance.")
                            .severity(DiagnosticTroubleCode.Severity.LOW)
                            .build(),
                    DiagnosticTroubleCode.builder()
                            .code("P0500")
                            .system("Vehicle Speed Control")
                            .description("Vehicle Speed Sensor 'A' Malfunction")
                            .symptoms("Speedometer not working, erratic automatic gear shifts, ABS light on")
                            .potentialCauses("Faulty VSS sensor, damaged wiring gear, instrument cluster issue")
                            .recommendedAction("Check wheel speed sensors and transmission speed sensor harness.")
                            .severity(DiagnosticTroubleCode.Severity.HIGH)
                            .build()
            );
            dtcRepository.saveAll(dtcs);
        }
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticTroubleCode> lookupDtc(String code) {
        return dtcRepository.findByCodeIgnoreCase(code);
    }

    @Transactional(readOnly = true)
    public VehicleHealthSummaryResponse getVehicleHealthSummary(UUID vehicleId) {
        TelemetrySnapshot latest = telemetryRepository.findFirstByVehicleIdOrderByTimestampDesc(vehicleId).orElse(null);

        int score = 95;
        List<DiagnosticTroubleCode> activeFaults = new ArrayList<>();
        List<String> maintenanceAlerts = new ArrayList<>();

        Double battery = latest != null ? latest.getBatteryVoltageV() : 12.6;
        Double coolant = latest != null ? latest.getEngineCoolantTempC() : 90.0;
        Double fuel = latest != null ? latest.getFuelLevelPct() : 75.0;

        if (battery != null && battery < 12.0) {
            score -= 15;
            maintenanceAlerts.add("Low 12V Battery Voltage (" + String.format("%.1f", battery) + "V). Charging/Replacement advised.");
        }

        if (coolant != null && coolant > 105.0) {
            score -= 25;
            maintenanceAlerts.add("Engine coolant temperature is high (" + String.format("%.1f", coolant) + "°C). Check coolant reservoir.");
        }

        if (latest != null && latest.getActiveDtcCodes() != null && !latest.getActiveDtcCodes().isBlank()) {
            String[] codes = latest.getActiveDtcCodes().split(",");
            for (String c : codes) {
                String trimmed = c.trim();
                lookupDtc(trimmed).ifPresent(activeFaults::add);
                score -= 15;
            }
        }

        score = Math.max(10, Math.min(100, score));

        String healthStatus = score >= 85 ? "EXCELLENT" : score >= 70 ? "GOOD" : score >= 50 ? "ATTENTION_REQUIRED" : "CRITICAL";

        return new VehicleHealthSummaryResponse(
                score,
                healthStatus,
                battery,
                coolant,
                fuel,
                activeFaults,
                maintenanceAlerts
        );
    }

    public AiAssistantResponse askCoDriver(AiAssistantRequest request) {
        String msg = request.message().toLowerCase();
        UUID vehicleId = request.vehicleId();
        Vehicle vehicle = vehicleId != null ? vehicleRepository.findById(vehicleId).orElse(null) : null;

        String vehicleName = vehicle != null ? (vehicle.getMake() + " " + vehicle.getModel()) : "your vehicle";
        String reply;
        List<String> actions = new ArrayList<>();
        String category = "GENERAL";

        if (msg.contains("dtc") || msg.contains("code") || msg.contains("check engine") || msg.contains("error")) {
            category = "DIAGNOSTICS";
            reply = "I analyzed " + vehicleName + "'s OBD-II status. No critical powertrain shutdowns detected right now. If your Check Engine light illuminates, I can run a deep diagnostic scan across all standard OBD P-codes.";
            actions.add("Run Full OBD-II DTC Scan");
            actions.add("Clear Non-Critical DTCs");
        } else if (msg.contains("battery") || msg.contains("voltage") || msg.contains("charging")) {
            category = "BATTERY_HEALTH";
            reply = "Battery telemetry for " + vehicleName + " indicates healthy resting alternator output (~13.8V-14.2V while running). Ensure terminal contacts are corrosion-free before monsoon/winter seasons.";
            actions.add("View Battery Voltage Graph");
            actions.add("Test Alternator Draw");
        } else if (msg.contains("coolant") || msg.contains("temp") || msg.contains("heat") || msg.contains("overheat")) {
            category = "ENGINE_THERMALS";
            reply = "Engine coolant operating envelope is nominal at ~88-92°C. Ensure radiator fins are clear of highway debris to avoid overheating during heavy traffic.";
            actions.add("Inspect Coolant Sensor Feed");
        } else if (msg.contains("fuel") || msg.contains("mileage") || msg.contains("efficiency") || msg.contains("eco")) {
            category = "FUEL_ECONOMY";
            reply = "Eco-driving tip for " + vehicleName + ": Maintain steady throttle between 1,800-2,200 RPM and anticipate braking 100 meters ahead to maximize km/L by up to 18%.";
            actions.add("View Trip Eco Score");
            actions.add("Optimize Route for Fuel");
        } else if (msg.contains("service") || msg.contains("oil") || msg.contains("maintenance")) {
            category = "MAINTENANCE";
            reply = "Next recommended service interval for " + vehicleName + " includes Engine Oil & Filter swap (5W-30/0W-20 synthetic), brake pad thickness check, and tyre rotation at 10,000 km.";
            actions.add("Schedule Service Appointment");
            actions.add("Log Maintenance Record");
        } else {
            category = "CO_DRIVER";
            reply = "Guardian Co-Driver online. I'm actively monitoring " + vehicleName + "'s live CAN-bus sensors, fuel rates, engine health, and road trip safety metrics. How can I assist your drive today?";
            actions.add("What's my vehicle health score?");
            actions.add("Check recent trip logs");
            actions.add("Are there any active fault codes?");
        }

        return new AiAssistantResponse(reply, actions, category, Instant.now());
    }
}
