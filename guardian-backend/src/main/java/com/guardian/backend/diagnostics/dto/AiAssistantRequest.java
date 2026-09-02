package com.guardian.backend.diagnostics.dto;

import java.util.UUID;

public record AiAssistantRequest(
        UUID vehicleId,
        String message,
        String context
) {
    public AiAssistantRequest(UUID vehicleId, String message) {
        this(vehicleId, message, null);
    }
}
