package com.velora.backend.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.backend.user.Role;
import com.velora.backend.user.User;
import com.velora.backend.user.UserRepository;
import com.velora.backend.vehicle.dto.CreateVehicleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private com.velora.backend.driver.DriverProfileRepository driverProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("tester@velora.ai").isEmpty()) {
            User user = User.builder()
                    .name("Driver Tester")
                    .email("tester@velora.ai")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.ROLE_DRIVER)
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    @WithMockUser(username = "tester@velora.ai")
    void shouldCreateAndRetrieveVehicle() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "Tata",
                "Nexon EV",
                "2024",
                "MAT12345678901234",
                "MH 02 AB 9999",
                "ISO 15765-4 CAN"
        );

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Tata"))
                .andExpect(jsonPath("$.model").value("Nexon EV"));

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Tata"));
    }
}
