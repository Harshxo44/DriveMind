package com.guardian.backend.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.backend.user.Role;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import com.guardian.backend.vehicle.dto.CreateVehicleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .name("Driver Tester")
                .email("tester@guardian.ai")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_DRIVER)
                .build();
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "tester@guardian.ai")
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
