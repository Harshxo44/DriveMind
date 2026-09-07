package com.guardian.backend.voice;

public interface VoiceProviderClient {

    /**
     * Transcribes raw audio bytes into text.
     * Implements Speech-to-Text (STT) capabilities.
     */
    String transcribeAudio(byte[] audioBytes) throws Exception;

    /**
     * Synthesizes text into high-fidelity speech audio bytes.
     * Implements Text-to-Speech (TTS) capabilities.
     * @return MP3 or PCM audio bytes encoded in base64.
     */
    String synthesizeSpeechBase64(String text) throws Exception;
}
