package com.guardian.backend.vehicle.dto;

import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String make,
        String model,
        String year,
        String vin,
        String registrationNumber,
        String obdProtocol,
        Instant createdAt,
        Instant updatedAt
) {}
