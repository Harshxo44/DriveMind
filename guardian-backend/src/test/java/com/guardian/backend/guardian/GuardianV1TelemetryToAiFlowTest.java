package com.guardian.backend.guardian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.backend.ai.AiConversationController;
import com.guardian.backend.ai.dto.*;
import com.guardian.backend.telemetry.TelemetryService;
import com.guardian.backend.telemetry.dto.IngestTelemetryRequest;
import com.guardian.backend.telemetry.dto.TelemetryResponse;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import com.guardian.backend.vehicle.Vehicle;
import com.guardian.backend.vehicle.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GuardianV1TelemetryToAiFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TelemetryService telemetryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void telemetryIngested_thenAiConversationReturnsVehicleHealth() throws Exception {
        User user = userRepository.findByEmail("flow_driver@drivemind.ai")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Flow Driver")
                        .email("flow_driver@drivemind.ai")
                        .password("hashed_pwd")
                        .build()));

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .owner(user)
                .make("Hyundai")
                .model("i20")
                .year("2021")
                .vin("VIN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .registrationNumber("MH" + (int)(Math.random() * 9000 + 1000))
                .build());

        // Simulate BLE tick -> backend ingest
        IngestTelemetryRequest ingest = new IngestTelemetryRequest(
                vehicle.getId(),
                null,
                68.0,
                2200,
                92.0,
                20.0,
                45.0,
                14.1,
                18.5204,
                73.8567,
                90.0,
                ""
        );

        TelemetryResponse saved = telemetryService.ingest(ingest);

        // Feed same telemetry into AI conversation request (matches current API design)
        AiConversationRequest aiRequest = new AiConversationRequest(
                "You are Guardian in-car AI.",
                new DriverContextDto("Harsh", "intermediate", "concise"),
                new VehicleContextDto(vehicle.getMake(), vehicle.getModel(), 2021, "Petrol"),
                new TelemetryContextDto(
                        saved.speedKmh(),
                        saved.rpm(),
                        saved.engineCoolantTempC(),
                        saved.batteryVoltageV(),
                        saved.fuelLevelPct(),
                        saved.throttlePositionPct(),
                        List.of()
                ),
                null,
                List.of(),
                "How is the car doing?"
        );

        mockMvc.perform(post("/api/v1/ai/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("VEHICLE_HEALTH"))
                .andExpect(jsonPath("$.spokenResponse").isNotEmpty())
                .andExpect(jsonPath("$.criticalAlert").value(false));
    }

    @Test
    void telemetryIngested_overheatTriggersSafetyShortCircuit() throws Exception {
        User user = userRepository.findByEmail("flow_driver2@drivemind.ai")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Flow Driver 2")
                        .email("flow_driver2@drivemind.ai")
                        .password("hashed_pwd")
                        .build()));

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .owner(user)
                .make("Hyundai")
                .model("i20")
                .year("2021")
                .vin("VIN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .registrationNumber("MH" + (int)(Math.random() * 9000 + 1000))
                .build());

        IngestTelemetryRequest ingest = new IngestTelemetryRequest(
                vehicle.getId(),
                null,
                80.0,
                3000,
                112.0,
                40.0,
                30.0,
                14.0,
                18.5204,
                73.8567,
                90.0,
                ""
        );

        TelemetryResponse saved = telemetryService.ingest(ingest);

        AiConversationRequest aiRequest = new AiConversationRequest(
                "You are Guardian in-car AI.",
                new DriverContextDto("Harsh", "intermediate", "concise"),
                new VehicleContextDto(vehicle.getMake(), vehicle.getModel(), 2021, "Petrol"),
                new TelemetryContextDto(
                        saved.speedKmh(),
                        saved.rpm(),
                        saved.engineCoolantTempC(),
                        saved.batteryVoltageV(),
                        saved.fuelLevelPct(),
                        saved.throttlePositionPct(),
                        List.of()
                ),
                null,
                List.of(),
                "How is everything?"
        );

        mockMvc.perform(post("/api/v1/ai/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aiRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalAlert").value(true))
                .andExpect(jsonPath("$.category").value("ENGINE_THERMAL"));
    }
}
