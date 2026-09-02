package com.guardian.backend.ai.dto;

import java.util.List;

public record TelemetryContextDto(
        Double speedKmh,
        Integer rpm,
        Double coolantTempC,
        Double batteryVoltage,
        Double fuelLevelPct,
        Double throttlePositionPct,
        List<String> dtcActive
) {}
