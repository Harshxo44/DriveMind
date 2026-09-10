package com.velora.backend.ai.dto;

import java.time.Instant;
import java.util.List;

public record AiConversationResponse(
        String spokenResponse,
        String displaySummary,
        String category,
        boolean criticalAlert,
        List<String> suggestedFollowUps,
        Instant timestamp
) {}
