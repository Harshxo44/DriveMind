package com.guardian.backend.ai.dto;

public record TripContextDto(
        Integer durationMinutes,
        Double distanceKm,
        String startLocation,
        String destination
) {}
