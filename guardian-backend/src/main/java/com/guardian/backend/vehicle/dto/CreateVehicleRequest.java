package com.guardian.backend.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(
        @NotBlank(message = "Make is required")
        @Size(max = 50, message = "Make must be under 50 chars")
        String make,

        @NotBlank(message = "Model is required")
        @Size(max = 50, message = "Model must be under 50 chars")
        String model,

        @Size(max = 4, message = "Year must be 4 characters")
        String year,

        @Size(max = 17, message = "VIN must be at most 17 characters")
        String vin,

        @Size(max = 20, message = "Registration number must be at most 20 characters")
        String registrationNumber,

        @Size(max = 30, message = "OBD protocol must be at most 30 characters")
        String obdProtocol
) {}
