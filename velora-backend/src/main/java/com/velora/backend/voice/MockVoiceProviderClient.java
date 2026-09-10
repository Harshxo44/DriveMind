package com.velora.backend.voice;

import org.springframework.stereotype.Component;

/**
 * Mock Voice Provider for reliable cost-free environment testing of the STT -> AI -> TTS pipeline.
 */
@Component
public class MockVoiceProviderClient implements VoiceProviderClient {

    @Override
    public String transcribeAudio(byte[] audioBytes) throws Exception {
        // Return a mock transcription based on byte length heuristics, or hardcoded for testing.
        return "how is the car doing"; // Default test query
    }

    @Override
    public String synthesizeSpeechBase64(String text) throws Exception {
        // In a real app, calls Deepgram Aura TTS API.
        // Here, we just return a fake base64 string prefix matching standard MP3 chunk bytes so the frontend can mock-play it.
        return "SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU5LjI3LjEwMAAAAAAAAAAAAAAA//tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASW5mbwAAAA8AAAAOAAAAVgA0NDA0NDAwNDAwODw8ODw8PDxAQEA0NDA0NDA=";
    }
}
