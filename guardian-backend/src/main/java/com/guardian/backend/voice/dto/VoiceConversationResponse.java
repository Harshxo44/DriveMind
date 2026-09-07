package com.guardian.backend.voice.dto;

import com.guardian.backend.ai.dto.AiConversationResponse;

public record VoiceConversationResponse(
        String transcript,              // What the STT heard the user say
        AiConversationResponse aiResponse, // The structured AI orchestrator response
        String base64AudioOut,          // TTS synthesized audio bytes (MP3/PCM)
        String ttsAudioUrl              // Optional streaming URL if using external TTS directly
) {}
