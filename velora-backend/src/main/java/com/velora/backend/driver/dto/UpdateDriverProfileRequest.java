package com.velora.backend.driver.dto;

import com.velora.backend.driver.DriverProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateDriverProfileRequest(
        @Size(max = 100)
        String displayName,

        DriverProfile.ExperienceLevel experienceLevel,

        DriverProfile.TonePreference tonePreference,

        @Min(40) @Max(200)
        Double speedAlertThresholdKmh,

        @Min(2000) @Max(7000)
        Integer highRpmThreshold,

        Boolean voiceGreetingEnabled
) {}
