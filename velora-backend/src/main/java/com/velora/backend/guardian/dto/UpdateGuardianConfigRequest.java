package com.velora.backend.guardian.dto;

public record UpdateGuardianConfigRequest(
        Boolean guardianModeEnabled,
        Boolean valetModeEnabled,
        Double maxSpeedLimitKmh,
        Boolean geofenceEnabled,
        Double geofenceCenterLat,
        Double geofenceCenterLng,
        Double geofenceRadiusMeters,
        Boolean towingAlertEnabled,
        Boolean crashAlertEnabled
) {}
