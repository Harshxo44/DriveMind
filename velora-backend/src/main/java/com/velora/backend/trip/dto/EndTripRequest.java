package com.velora.backend.trip.dto;

import java.util.UUID;

public record EndTripRequest(
        String endLocation,
        Double distanceKm,
        Double avgSpeedKmh,
        Double maxSpeedKmh,
        Double fuelConsumedLiters,
        Integer harshBrakingCount,
        Integer harshAccelerationCount
) {}
