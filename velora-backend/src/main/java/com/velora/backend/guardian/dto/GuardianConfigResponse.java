package com.velora.backend.guardian.dto;

import java.time.Instant;
import java.util.UUID;

public record GuardianConfigResponse(
        UUID id,
        UUID vehicleId,
        boolean guardianModeEnabled,
        boolean valetModeEnabled,
        Double maxSpeedLimitKmh,
        boolean geofenceEnabled,
        Double geofenceCenterLat,
        Double geofenceCenterLng,
        Double geofenceRadiusMeters,
        boolean towingAlertEnabled,
        boolean crashAlertEnabled,
        Instant updatedAt
) {}
