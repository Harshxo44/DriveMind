package com.guardian.backend.voice;

import com.guardian.backend.ai.AiConversationService;
import com.guardian.backend.ai.dto.AiConversationRequest;
import com.guardian.backend.ai.dto.AiConversationResponse;
import com.guardian.backend.ai.dto.TelemetryContextDto;
import com.guardian.backend.telemetry.TelemetryService;
import com.guardian.backend.telemetry.dto.TelemetryResponse;
import com.guardian.backend.voice.dto.VoiceConversationRequest;
import com.guardian.backend.voice.dto.VoiceConversationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class VoiceService {

    private static final Logger log = LoggerFactory.getLogger(VoiceService.class);

    private final VoiceProviderClient voiceProviderClient;
    private final AiConversationService aiConversationService;
    private final TelemetryService telemetryService;

    public VoiceService(VoiceProviderClient voiceProviderClient, AiConversationService aiConversationService, TelemetryService telemetryService) {
        this.voiceProviderClient = voiceProviderClient;
        this.aiConversationService = aiConversationService;
        this.telemetryService = telemetryService;
    }

    /**
     * E2E Voice Pipeline:
     * 1. STT: Decode Base64 audio -> Transcribe to text
     * 2. Context Injection: Fetch latest live BLE telemetry snapshot if omitted
     * 3. Orchestration: Inject transcription, telemetry, and intent into the AI orchestrator
     * 4. TTS: Synthesize the AI's spokenReply back into audio payload
     */
    public VoiceConversationResponse processVoiceConversation(VoiceConversationRequest request) {
        try {
            // Priority STT Pipeline (Transcribe Audio Box)
            String transcript = "";
            if (request.base64AudioIn() != null && !request.base64AudioIn().isBlank()) {
                byte[] audioBytes = Base64.getDecoder().decode(request.base64AudioIn());
                transcript = voiceProviderClient.transcribeAudio(audioBytes);
                log.info("STT Transcription Complete: '{}'", transcript);
            } else {
                throw new IllegalArgumentException("Voice pipeline requires base64 audio payload input.");
            }

            // Fallback context builder if the Mobile App didn't push all telemetry explicitly in the prompt:
            TelemetryContextDto telemetryDto = request.telemetry();
            if (telemetryDto == null && request.vehicleId() != null) {
                TelemetryResponse liveSnapshot = telemetryService.getLatestForVehicle(request.vehicleId());
                if (liveSnapshot != null) {
                    telemetryDto = new TelemetryContextDto(
                            liveSnapshot.speedKmh(),
                            liveSnapshot.rpm(),
                            liveSnapshot.engineCoolantTempC(),
                            liveSnapshot.batteryVoltageV(),
                            liveSnapshot.fuelLevelPct(),
                            liveSnapshot.throttlePositionPct(),
                            java.util.List.of()
                    );
                }
            }

            AiConversationRequest aiReq = new AiConversationRequest(
                    request.systemInstruction() != null ? request.systemInstruction() : "You are Guardian in-car AI.",
                    request.driver(),
                    request.vehicle(),
                    telemetryDto,
                    request.trip(),
                    request.ephemeralMemory(),
                    transcript
            );

            // Guardian AI Orchestrator Call
            AiConversationResponse orchestratorResponse = aiConversationService.processConversation(aiReq);
            log.info("AI Orchestration Complete. Category: {}. Intended Spoken Reply: {}", orchestratorResponse.category(), orchestratorResponse.spokenResponse());

            // TTS Pipeline (Synthesize Audio Output Box)
            String ttsBase64Out = "";
            if (orchestratorResponse.spokenResponse() != null && !orchestratorResponse.spokenResponse().isBlank()) {
                ttsBase64Out = voiceProviderClient.synthesizeSpeechBase64(orchestratorResponse.spokenResponse());
            }

            return new VoiceConversationResponse(transcript, orchestratorResponse, ttsBase64Out, null);
        } catch (Exception e) {
            log.error("Pipeline failure in VoiceService E2E processing: ", e);
            throw new RuntimeException("Voice interaction failure", e);
        }
    }
}
