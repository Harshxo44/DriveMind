package com.guardian.backend.device.dto;

import com.guardian.backend.device.Device;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String serialNumber,
        String firmwareVersion,
        String hardwareVersion,
        Device.DeviceStatus status,
        UUID userId,
        UUID vehicleId,
        String vehicleMake,
        String vehicleModel,
        Instant lastSeenAt,
        Instant createdAt,
        Instant updatedAt
) {}
