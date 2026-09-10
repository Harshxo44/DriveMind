package com.velora.backend.telemetry.dto;

import java.time.Instant;
import java.util.UUID;

public record TelemetryResponse(
        UUID id,
        UUID vehicleId,
        UUID tripId,
        Double speedKmh,
        Integer rpm,
        Double engineCoolantTempC,
        Double throttlePositionPct,
        Double fuelLevelPct,
        Double batteryVoltageV,
        Double latitude,
        Double longitude,
        Double heading,
        String activeDtcCodes,
        Instant timestamp
) {}
