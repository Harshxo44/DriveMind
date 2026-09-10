package com.velora.backend.trip.dto;

import com.velora.backend.trip.Trip;

import java.time.Instant;
import java.util.UUID;

public record TripResponse(
        UUID id,
        UUID vehicleId,
        String vehicleMake,
        String vehicleModel,
        UUID driverId,
        String driverName,
        Instant startTime,
        Instant endTime,
        Double distanceKm,
        Double avgSpeedKmh,
        Double maxSpeedKmh,
        Double fuelConsumedLiters,
        Integer harshBrakingCount,
        Integer harshAccelerationCount,
        Integer safetyScore,
        Trip.TripStatus status,
        String startLocation,
        String endLocation,
        Instant createdAt
) {}
