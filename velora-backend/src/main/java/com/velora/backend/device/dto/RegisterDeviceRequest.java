package com.velora.backend.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDeviceRequest(
        @NotBlank(message = "Serial number is required")
        @Size(max = 64, message = "Serial number max 64 chars")
        String serialNumber,

        @Size(max = 30)
        String firmwareVersion,

        @Size(max = 30)
        String hardwareVersion
) {}
