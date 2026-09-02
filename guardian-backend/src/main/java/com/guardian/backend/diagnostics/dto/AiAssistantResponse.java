package com.guardian.backend.diagnostics.dto;

import java.time.Instant;
import java.util.List;

public record AiAssistantResponse(
        String reply,
        List<String> suggestedActions,
        String category,
        Instant timestamp
) {}
