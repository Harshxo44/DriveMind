package com.velora.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AiConversationRequest(
        String systemInstruction,
        DriverContextDto driver,
        VehicleContextDto vehicle,
        TelemetryContextDto telemetry,
        TripContextDto trip,
        List<String> ephemeralMemory,
        @NotBlank String userQuery
) {}
