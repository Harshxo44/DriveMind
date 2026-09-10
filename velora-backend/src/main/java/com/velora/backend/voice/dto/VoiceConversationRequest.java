package com.velora.backend.voice.dto;

import com.velora.backend.ai.dto.DriverContextDto;
import com.velora.backend.ai.dto.TelemetryContextDto;
import com.velora.backend.ai.dto.TripContextDto;
import com.velora.backend.ai.dto.VehicleContextDto;

import java.util.List;
import java.util.UUID;

public record VoiceConversationRequest(
        String base64AudioIn, // base64 encoded audio from the microphone
        UUID vehicleId,
        UUID driverId,
        String systemInstruction,
        DriverContextDto driver,
        VehicleContextDto vehicle,
        TelemetryContextDto telemetry,
        TripContextDto trip,
        List<String> ephemeralMemory
) {}
