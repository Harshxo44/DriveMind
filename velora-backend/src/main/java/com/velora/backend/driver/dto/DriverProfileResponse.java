package com.velora.backend.driver.dto;

import com.velora.backend.driver.DriverProfile;

import java.time.Instant;
import java.util.UUID;

public record DriverProfileResponse(
        UUID id,
        UUID userId,
        String displayName,
        DriverProfile.ExperienceLevel experienceLevel,
        DriverProfile.TonePreference tonePreference,
        Double speedAlertThresholdKmh,
        Integer highRpmThreshold,
        boolean voiceGreetingEnabled,
        Instant updatedAt
) {
    public static DriverProfileResponse fromEntity(DriverProfile profile) {
        return new DriverProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getDisplayName(),
                profile.getExperienceLevel(),
                profile.getTonePreference(),
                profile.getSpeedAlertThresholdKmh(),
                profile.getHighRpmThreshold(),
                profile.isVoiceGreetingEnabled(),
                profile.getUpdatedAt()
        );
    }
}
