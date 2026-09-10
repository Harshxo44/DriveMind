package com.velora.backend.ai.dto;

public record VehicleContextDto(
        String make,
        String model,
        Integer year,
        String fuelType
) {}
