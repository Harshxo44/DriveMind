package com.velora.backend.diagnostics.dto;

import com.velora.backend.diagnostics.DiagnosticTroubleCode;

import java.util.List;

public record VehicleHealthSummaryResponse(
        int overallScore,
        String healthStatus,
        Double batteryVoltage,
        Double coolantTempC,
        Double fuelLevelPct,
        List<DiagnosticTroubleCode> activeFaults,
        List<String> maintenanceAlerts
) {}
