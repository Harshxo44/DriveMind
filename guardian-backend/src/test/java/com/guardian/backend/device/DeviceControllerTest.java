package com.guardian.backend.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.backend.device.dto.PairDeviceRequest;
import com.guardian.backend.device.dto.RegisterDeviceRequest;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import com.guardian.backend.vehicle.Vehicle;
import com.guardian.backend.vehicle.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private User testUser;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("driver@drivemind.ai")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Guardian Driver")
                        .email("driver@drivemind.ai")
                        .password("hashed_pwd")
                        .build()));

        testVehicle = vehicleRepository.save(Vehicle.builder()
                .owner(testUser)
                .make("Hyundai")
                .model("i20")
                .year("2021")
                .vin("MALBB51BLAM" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .registrationNumber("MH02AB" + (int)(Math.random() * 9000 + 1000))
                .build());
    }

    @Test
    @WithMockUser(username = "driver@drivemind.ai")
    void shouldRegisterAndPairDeviceSuccessfully() throws Exception {
        String serial = "GD-ESP32-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        RegisterDeviceRequest registerRequest = new RegisterDeviceRequest(
                serial,
                "v1.0.0",
                "v1.0"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value(serial))
                .andExpect(jsonPath("$.status").value("UNPAIRED"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String deviceId = objectMapper.readTree(responseJson).get("id").asText();

        // Pair device
        PairDeviceRequest pairRequest = new PairDeviceRequest(testVehicle.getId());
        mockMvc.perform(post("/api/v1/devices/" + deviceId + "/pair")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pairRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAIRED"))
                .andExpect(jsonPath("$.vehicleId").value(testVehicle.getId().toString()));

        // Heartbeat
        mockMvc.perform(post("/api/v1/devices/" + deviceId + "/heartbeat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Get devices for user
        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
