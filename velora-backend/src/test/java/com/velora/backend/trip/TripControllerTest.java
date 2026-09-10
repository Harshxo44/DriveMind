package com.velora.backend.trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.backend.trip.dto.EndTripRequest;
import com.velora.backend.trip.dto.StartTripRequest;
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
class TripControllerTest {

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
                .make("Maruti Suzuki")
                .model("Swift")
                .year("2022")
                .vin("MA3E" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .registrationNumber("DL01EF" + (int)(Math.random() * 9000 + 1000))
                .build());
    }

    @Test
    @WithMockUser(username = "driver@drivemind.ai")
    void shouldStartAndEndTripSuccessfully() throws Exception {
        StartTripRequest startRequest = new StartTripRequest(
                testVehicle.getId(),
                "Home"
        );

        MvcResult startResult = mockMvc.perform(post("/api/v1/trips/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startLocation").value("Home"))
                .andReturn();

        String tripId = objectMapper.readTree(startResult.getResponse().getContentAsString()).get("id").asText();

        EndTripRequest endRequest = new EndTripRequest(
                "Office",
                18.5,
                45.0,
                82.0,
                1.2,
                0,
                0
        );

        mockMvc.perform(post("/api/v1/trips/" + tripId + "/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.distanceKm").value(18.5));

        // Retrieve trip
        mockMvc.perform(get("/api/v1/trips/" + tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tripId));

        // Retrieve driver trips
        mockMvc.perform(get("/api/v1/trips/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
