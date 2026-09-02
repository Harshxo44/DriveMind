package com.guardian.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.backend.ai.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAnswerVehicleHealthQueryWithTelemetry() throws Exception {
        AiConversationRequest request = new AiConversationRequest(
                "You are Guardian in-car AI.",
                new DriverContextDto("Harsh", "intermediate", "concise"),
                new VehicleContextDto("Hyundai", "i20", 2021, "Petrol"),
                new TelemetryContextDto(68.0, 2200, 92.0, 14.1, 45.0, 20.0, List.of()),
                new TripContextDto(20, 14.5, "Home", "Office"),
                List.of("Remember to pick up groceries"),
                "How is the car doing?"
        );

        mockMvc.perform(post("/api/v1/ai/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("VEHICLE_HEALTH"))
                .andExpect(jsonPath("$.spokenResponse").isNotEmpty())
                .andExpect(jsonPath("$.criticalAlert").value(false));
    }

    @Test
    void shouldTriggerCriticalOverheatingAlert() throws Exception {
        AiConversationRequest request = new AiConversationRequest(
                "You are Guardian in-car AI.",
                new DriverContextDto("Harsh", "intermediate", "concise"),
                new VehicleContextDto("Hyundai", "i20", 2021, "Petrol"),
                new TelemetryContextDto(80.0, 3000, 112.0, 14.0, 30.0, 40.0, List.of()),
                new TripContextDto(45, 38.0, "Highway", "City"),
                List.of(),
                "How is everything?"
        );

        mockMvc.perform(post("/api/v1/ai/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalAlert").value(true))
                .andExpect(jsonPath("$.category").value("ENGINE_THERMAL"));
    }
}
