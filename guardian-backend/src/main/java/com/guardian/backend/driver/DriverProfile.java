package com.guardian.backend.driver;

import com.guardian.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "driver_profiles")
public class DriverProfile {

    public enum ExperienceLevel {
        BEGINNER, INTERMEDIATE, EXPERIENCED
    }

    public enum TonePreference {
        CONCISE, CALM, DETAILED, ENERGETIC
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel = ExperienceLevel.INTERMEDIATE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TonePreference tonePreference = TonePreference.CONCISE;

    private Double speedAlertThresholdKmh = 80.0;

    private Integer highRpmThreshold = 3500;

    private boolean voiceGreetingEnabled = true;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public DriverProfile() {}

    public DriverProfile(UUID id, User user, String displayName, ExperienceLevel experienceLevel,
                         TonePreference tonePreference, Double speedAlertThresholdKmh,
                         Integer highRpmThreshold, boolean voiceGreetingEnabled,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.displayName = displayName;
        this.experienceLevel = experienceLevel != null ? experienceLevel : ExperienceLevel.INTERMEDIATE;
        this.tonePreference = tonePreference != null ? tonePreference : TonePreference.CONCISE;
        this.speedAlertThresholdKmh = speedAlertThresholdKmh != null ? speedAlertThresholdKmh : 80.0;
        this.highRpmThreshold = highRpmThreshold != null ? highRpmThreshold : 3500;
        this.voiceGreetingEnabled = voiceGreetingEnabled;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static DriverProfileBuilder builder() {
        return new DriverProfileBuilder();
    }

    public static class DriverProfileBuilder {
        private UUID id;
        private User user;
        private String displayName;
        private ExperienceLevel experienceLevel = ExperienceLevel.INTERMEDIATE;
        private TonePreference tonePreference = TonePreference.CONCISE;
        private Double speedAlertThresholdKmh = 80.0;
        private Integer highRpmThreshold = 3500;
        private boolean voiceGreetingEnabled = true;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public DriverProfileBuilder id(UUID id) { this.id = id; return this; }
        public DriverProfileBuilder user(User user) { this.user = user; return this; }
        public DriverProfileBuilder displayName(String displayName) { this.displayName = displayName; return this; }
        public DriverProfileBuilder experienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; return this; }
        public DriverProfileBuilder tonePreference(TonePreference tonePreference) { this.tonePreference = tonePreference; return this; }
        public DriverProfileBuilder speedAlertThresholdKmh(Double speedAlertThresholdKmh) { this.speedAlertThresholdKmh = speedAlertThresholdKmh; return this; }
        public DriverProfileBuilder highRpmThreshold(Integer highRpmThreshold) { this.highRpmThreshold = highRpmThreshold; return this; }
        public DriverProfileBuilder voiceGreetingEnabled(boolean voiceGreetingEnabled) { this.voiceGreetingEnabled = voiceGreetingEnabled; return this; }
        public DriverProfileBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public DriverProfileBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public DriverProfile build() {
            return new DriverProfile(id, user, displayName, experienceLevel, tonePreference,
                    speedAlertThresholdKmh, highRpmThreshold, voiceGreetingEnabled, createdAt, updatedAt);
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }
    public TonePreference getTonePreference() { return tonePreference; }
    public void setTonePreference(TonePreference tonePreference) { this.tonePreference = tonePreference; }
    public Double getSpeedAlertThresholdKmh() { return speedAlertThresholdKmh; }
    public void setSpeedAlertThresholdKmh(Double speedAlertThresholdKmh) { this.speedAlertThresholdKmh = speedAlertThresholdKmh; }
    public Integer getHighRpmThreshold() { return highRpmThreshold; }
    public void setHighRpmThreshold(Integer highRpmThreshold) { this.highRpmThreshold = highRpmThreshold; }
    public boolean isVoiceGreetingEnabled() { return voiceGreetingEnabled; }
    public void setVoiceGreetingEnabled(boolean voiceGreetingEnabled) { this.voiceGreetingEnabled = voiceGreetingEnabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
