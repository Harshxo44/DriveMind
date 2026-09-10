package com.velora.backend.ai.dto;

import java.util.List;

public record DriverContextDto(
        String name,
        String experienceLevel,
        String tonePreference
) {}
