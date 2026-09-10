package com.velora.backend.device.dto;

import java.util.UUID;

public record PairDeviceRequest(
        UUID vehicleId
) {}
