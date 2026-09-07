package com.guardian.backend.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.backend.ai.dto.DriverContextDto;
import com.guardian.backend.ai.dto.TelemetryContextDto;
import com.guardian.backend.ai.dto.VehicleContextDto;
import com.guardian.backend.voice.dto.VoiceConversationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessEndToEndVoicePipelineSuccessfully() throws Exception {
        byte[] dummyAudioBytes = "Fake audio PCM bytes recorded from phone mic".getBytes();
        String base64Audio = Base64.getEncoder().encodeToString(dummyAudioBytes);

        VoiceConversationRequest request = new VoiceConversationRequest(
                base64Audio,
                null,
                null,
                "You are Guardian in-car AI.",
                new DriverContextDto("Harsh", "intermediate", "concise"),
                new VehicleContextDto("Hyundai", "i20", 2021, "Petrol"),
                new TelemetryContextDto(65.0, 2100, 91.0, 14.2, 50.0, 25.0, List.of()),
                null,
                List.of()
        );

        mockMvc.perform(post("/api/v1/voice/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcript").value("how is the car doing"))
                .andExpect(jsonPath("$.aiResponse.category").value("VEHICLE_HEALTH"))
                .andExpect(jsonPath("$.aiResponse.spokenResponse").isNotEmpty())
                .andExpect(jsonPath("$.base64AudioOut").isNotEmpty());
    }
}
