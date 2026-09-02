package com.guardian.backend.driver;

import com.guardian.backend.driver.dto.DriverProfileResponse;
import com.guardian.backend.driver.dto.UpdateDriverProfileRequest;
import com.guardian.backend.exception.ResourceNotFoundException;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DriverProfileService {

    private final DriverProfileRepository profileRepository;
    private final UserRepository userRepository;

    public DriverProfileService(DriverProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DriverProfileResponse getOrCreateProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        DriverProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    DriverProfile newProfile = DriverProfile.builder()
                            .user(user)
                            .displayName(user.getName())
                            .experienceLevel(DriverProfile.ExperienceLevel.INTERMEDIATE)
                            .tonePreference(DriverProfile.TonePreference.CONCISE)
                            .speedAlertThresholdKmh(80.0)
                            .highRpmThreshold(3500)
                            .voiceGreetingEnabled(true)
                            .build();
                    return profileRepository.save(newProfile);
                });

        return DriverProfileResponse.fromEntity(profile);
    }

    @Transactional
    public DriverProfileResponse updateProfile(UUID userId, UpdateDriverProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        DriverProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> DriverProfile.builder()
                        .user(user)
                        .displayName(user.getName())
                        .build());

        if (request.displayName() != null && !request.displayName().isBlank()) {
            profile.setDisplayName(request.displayName());
        }
        if (request.experienceLevel() != null) {
            profile.setExperienceLevel(request.experienceLevel());
        }
        if (request.tonePreference() != null) {
            profile.setTonePreference(request.tonePreference());
        }
        if (request.speedAlertThresholdKmh() != null) {
            profile.setSpeedAlertThresholdKmh(request.speedAlertThresholdKmh());
        }
        if (request.highRpmThreshold() != null) {
            profile.setHighRpmThreshold(request.highRpmThreshold());
        }
        if (request.voiceGreetingEnabled() != null) {
            profile.setVoiceGreetingEnabled(request.voiceGreetingEnabled());
        }

        DriverProfile saved = profileRepository.save(profile);
        return DriverProfileResponse.fromEntity(saved);
    }
}
