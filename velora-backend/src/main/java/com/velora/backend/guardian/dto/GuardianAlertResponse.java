package com.velora.backend.guardian.dto;

import com.velora.backend.guardian.GuardianAlert;

import java.time.Instant;
import java.util.UUID;

public record GuardianAlertResponse(
        UUID id,
        UUID vehicleId,
        GuardianAlert.AlertType alertType,
        GuardianAlert.AlertSeverity severity,
        String title,
        String description,
        Double latitude,
        Double longitude,
        boolean acknowledged,
        Instant timestamp
) {}
