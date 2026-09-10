package com.velora.backend.guardian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.backend.guardian.dto.CreateAlertRequest;
import com.velora.backend.guardian.dto.UpdateGuardianConfigRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GuardianControllerTest {

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
        User testUser = userRepository.findByEmail("driver@drivemind.ai")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Guardian Driver")
                        .email("driver@drivemind.ai")
                        .password("hashed_pwd")
                        .build()));

        testVehicle = vehicleRepository.save(Vehicle.builder()
                .owner(testUser)
                .make("Kia")
                .model("Seltos")
                .year("2022")
                .vin("KNAG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .registrationNumber("KA01GH" + (int)(Math.random() * 9000 + 1000))
                .build());
    }

    @Test
    void shouldManageGuardianConfigAndAlerts() throws Exception {
        // 1. Get Config
        mockMvc.perform(get("/api/v1/guardian/config/" + testVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(testVehicle.getId().toString()))
                .andExpect(jsonPath("$.maxSpeedLimitKmh").isNumber());

        // 2. Update Config
        UpdateGuardianConfigRequest updateReq = new UpdateGuardianConfigRequest(
                true,
                false,
                130.0,
                false,
                null,
                null,
                null,
                true,
                true
        );

        mockMvc.perform(put("/api/v1/guardian/config/" + testVehicle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxSpeedLimitKmh").value(130.0));

        // 3. Create Alert
        CreateAlertRequest alertReq = new CreateAlertRequest(
                testVehicle.getId(),
                GuardianAlert.AlertType.ENGINE_OVERHEAT,
                GuardianAlert.AlertSeverity.HIGH,
                "Engine Coolant Overheat",
                "Engine coolant reached 108C",
                18.5204,
                73.8567
        );

        MvcResult alertResult = mockMvc.perform(post("/api/v1/guardian/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alertReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertType").value("ENGINE_OVERHEAT"))
                .andExpect(jsonPath("$.acknowledged").value(false))
                .andReturn();

        String alertId = objectMapper.readTree(alertResult.getResponse().getContentAsString()).get("id").asText();

        // 4. Acknowledge Alert
        mockMvc.perform(post("/api/v1/guardian/alerts/" + alertId + "/ack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true));

        // 5. List Alerts
        mockMvc.perform(get("/api/v1/guardian/alerts/" + testVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
