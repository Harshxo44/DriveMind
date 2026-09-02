package com.guardian.backend.privacy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PrivacyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "driver@drivemind.ai")
    void shouldGetPrivacySummary() throws Exception {
        mockMvc.perform(get("/api/v1/privacy/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataRetentionPolicy").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "driver@drivemind.ai")
    void shouldPurgeUserDataSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/privacy/purge-my-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
