package com.velora.backend.ai.dto;

public record TripContextDto(
        Integer durationMinutes,
        Double distanceKm,
        String startLocation,
        String destination
) {}
