package com.velora.backend.trip.dto;

import java.util.UUID;

public record StartTripRequest(
        UUID vehicleId,
        String startLocation
) {}
