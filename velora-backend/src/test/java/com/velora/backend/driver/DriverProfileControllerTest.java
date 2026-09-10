package com.velora.backend.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.backend.driver.dto.UpdateDriverProfileRequest;
import com.velora.backend.user.Role;
import com.velora.backend.user.User;
import com.velora.backend.user.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DriverProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        driverProfileRepository.deleteAll();
        if (userRepository.findByEmail("driver@drivemind.ai").isEmpty()) {
            User user = User.builder()
                    .name("Guardian Driver")
                    .email("driver@drivemind.ai")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.ROLE_DRIVER)
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    @WithMockUser(username = "driver@drivemind.ai")
    void shouldGetOrCreateProfile() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").isNotEmpty())
                .andExpect(jsonPath("$.speedAlertThresholdKmh").value(80.0));
    }

    @Test
    @WithMockUser(username = "driver@drivemind.ai")
    void shouldUpdateProfilePreferences() throws Exception {
        UpdateDriverProfileRequest request = new UpdateDriverProfileRequest(
                "Harsh (Track Mode)",
                DriverProfile.ExperienceLevel.EXPERIENCED,
                DriverProfile.TonePreference.CALM,
                100.0,
                4500,
                true
        );

        mockMvc.perform(put("/api/v1/drivers/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Harsh (Track Mode)"))
                .andExpect(jsonPath("$.speedAlertThresholdKmh").value(100.0))
                .andExpect(jsonPath("$.highRpmThreshold").value(4500));
    }
}
