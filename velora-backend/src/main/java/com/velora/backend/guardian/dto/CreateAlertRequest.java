package com.velora.backend.guardian.dto;

import com.velora.backend.guardian.GuardianAlert;

import java.util.UUID;

public record CreateAlertRequest(
        UUID vehicleId,
        GuardianAlert.AlertType alertType,
        GuardianAlert.AlertSeverity severity,
        String title,
        String description,
        Double latitude,
        Double longitude
) {}
