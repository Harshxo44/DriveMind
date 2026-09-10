package com.velora.backend.voice;

import com.velora.backend.voice.dto.VoiceConversationRequest;
import com.velora.backend.voice.dto.VoiceConversationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/voice")
public class VoiceController {

    private final VoiceService voiceService;

    public VoiceController(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    /**
     * Endpoint for Complete Voice Pipeline Interaction:
     * Base64 Audio Input -> STT -> Grounded AI Orchestration -> TTS -> Base64 Audio Output.
     */
    @PostMapping("/conversation")
    public ResponseEntity<VoiceConversationResponse> handleVoiceConversation(
            @Valid @RequestBody VoiceConversationRequest request) {
        return ResponseEntity.ok(voiceService.processVoiceConversation(request));
    }
}
