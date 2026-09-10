package com.velora.backend.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.backend.telemetry.dto.IngestTelemetryRequest;
import com.velora.backend.user.User;
import com.velora.backend.user.UserRepository;
import com.velora.backend.vehicle.Vehicle;
import com.velora.backend.vehicle.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        User testUser = userRepository.findByEmail("telemetry_driver@drivemind.ai")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Telemetry Driver")
                        .email("telemetry_driver@drivemind.ai")
                        .password("hashed_pwd")
                        .build()));

        testVehicle = vehicleRepository.save(Vehicle.builder()
                .owner(testUser)
                .make("Tata")
                .model("Nexon")
                .year("2023")
                .vin("MAT624" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .registrationNumber("MH12CD" + (int)(Math.random() * 9000 + 1000))
                .build());
    }

    @Test
    void shouldIngestAndRetrieveVehicleTelemetry() throws Exception {
        IngestTelemetryRequest request = new IngestTelemetryRequest(
                testVehicle.getId(),
                null,
                55.0,
                1800,
                88.5,
                18.0,
                65.0,
                14.2,
                18.5204,
                73.8567,
                90.0,
                "P0171"
        );

        // 1. Ingest telemetry
        mockMvc.perform(post("/api/v1/telemetry/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.speedKmh").value(55.0))
                .andExpect(jsonPath("$.rpm").value(1800))
                .andExpect(jsonPath("$.batteryVoltageV").value(14.2));

        // 2. Query latest telemetry
        mockMvc.perform(get("/api/v1/telemetry/vehicle/" + testVehicle.getId() + "/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speedKmh").value(55.0))
                .andExpect(jsonPath("$.engineCoolantTempC").value(88.5));

        // 3. Query recent telemetry
        mockMvc.perform(get("/api/v1/telemetry/vehicle/" + testVehicle.getId() + "/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].vehicleId").value(testVehicle.getId().toString()));
    }
}
