package com.velora.backend.reminder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.backend.reminder.dto.CreateReminderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "driver@drivemind.ai")
    void shouldCreateAndRetrieveReminder() throws Exception {
        CreateReminderRequest request = new CreateReminderRequest(
                null,
                "Engine Oil Change (10,000 km)",
                Reminder.ReminderType.MAINTENANCE,
                10000.0,
                Instant.now().plus(30, ChronoUnit.DAYS),
                "Use 5W-30 Synthetic Oil"
        );

        mockMvc.perform(post("/api/v1/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Engine Oil Change (10,000 km)"))
                .andExpect(jsonPath("$.reminderType").value("MAINTENANCE"));

        mockMvc.perform(get("/api/v1/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
