package com.velora.backend.guardian;

import com.velora.backend.vehicle.Vehicle;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "guardian_configs")
public class GuardianConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, unique = true)
    private Vehicle vehicle;

    private boolean guardianModeEnabled = false;
    private boolean valetModeEnabled = false;
    private Double maxSpeedLimitKmh = 80.0;
    private boolean geofenceEnabled = false;
    private Double geofenceCenterLat;
    private Double geofenceCenterLng;
    private Double geofenceRadiusMeters;
    private boolean towingAlertEnabled = true;
    private boolean crashAlertEnabled = true;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public GuardianConfig() {}

    public GuardianConfig(UUID id, Vehicle vehicle, boolean guardianModeEnabled, boolean valetModeEnabled, Double maxSpeedLimitKmh, boolean geofenceEnabled, Double geofenceCenterLat, Double geofenceCenterLng, Double geofenceRadiusMeters, boolean towingAlertEnabled, boolean crashAlertEnabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.vehicle = vehicle;
        this.guardianModeEnabled = guardianModeEnabled;
        this.valetModeEnabled = valetModeEnabled;
        this.maxSpeedLimitKmh = maxSpeedLimitKmh != null ? maxSpeedLimitKmh : 80.0;
        this.geofenceEnabled = geofenceEnabled;
        this.geofenceCenterLat = geofenceCenterLat;
        this.geofenceCenterLng = geofenceCenterLng;
        this.geofenceRadiusMeters = geofenceRadiusMeters;
        this.towingAlertEnabled = towingAlertEnabled;
        this.crashAlertEnabled = crashAlertEnabled;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static GuardianConfigBuilder builder() {
        return new GuardianConfigBuilder();
    }

    public static class GuardianConfigBuilder {
        private UUID id;
        private Vehicle vehicle;
        private boolean guardianModeEnabled = false;
        private boolean valetModeEnabled = false;
        private Double maxSpeedLimitKmh = 80.0;
        private boolean geofenceEnabled = false;
        private Double geofenceCenterLat;
        private Double geofenceCenterLng;
        private Double geofenceRadiusMeters;
        private boolean towingAlertEnabled = true;
        private boolean crashAlertEnabled = true;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public GuardianConfigBuilder id(UUID id) { this.id = id; return this; }
        public GuardianConfigBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public GuardianConfigBuilder guardianModeEnabled(boolean guardianModeEnabled) { this.guardianModeEnabled = guardianModeEnabled; return this; }
        public GuardianConfigBuilder valetModeEnabled(boolean valetModeEnabled) { this.valetModeEnabled = valetModeEnabled; return this; }
        public GuardianConfigBuilder maxSpeedLimitKmh(Double maxSpeedLimitKmh) { this.maxSpeedLimitKmh = maxSpeedLimitKmh; return this; }
        public GuardianConfigBuilder geofenceEnabled(boolean geofenceEnabled) { this.geofenceEnabled = geofenceEnabled; return this; }
        public GuardianConfigBuilder geofenceCenterLat(Double geofenceCenterLat) { this.geofenceCenterLat = geofenceCenterLat; return this; }
        public GuardianConfigBuilder geofenceCenterLng(Double geofenceCenterLng) { this.geofenceCenterLng = geofenceCenterLng; return this; }
        public GuardianConfigBuilder geofenceRadiusMeters(Double geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; return this; }
        public GuardianConfigBuilder towingAlertEnabled(boolean towingAlertEnabled) { this.towingAlertEnabled = towingAlertEnabled; return this; }
        public GuardianConfigBuilder crashAlertEnabled(boolean crashAlertEnabled) { this.crashAlertEnabled = crashAlertEnabled; return this; }
        public GuardianConfigBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public GuardianConfigBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public GuardianConfig build() {
            return new GuardianConfig(id, vehicle, guardianModeEnabled, valetModeEnabled, maxSpeedLimitKmh, geofenceEnabled, geofenceCenterLat, geofenceCenterLng, geofenceRadiusMeters, towingAlertEnabled, crashAlertEnabled, createdAt, updatedAt);
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public boolean isGuardianModeEnabled() { return guardianModeEnabled; }
    public void setGuardianModeEnabled(boolean guardianModeEnabled) { this.guardianModeEnabled = guardianModeEnabled; }
    public boolean isValetModeEnabled() { return valetModeEnabled; }
    public void setValetModeEnabled(boolean valetModeEnabled) { this.valetModeEnabled = valetModeEnabled; }
    public Double getMaxSpeedLimitKmh() { return maxSpeedLimitKmh; }
    public void setMaxSpeedLimitKmh(Double maxSpeedLimitKmh) { this.maxSpeedLimitKmh = maxSpeedLimitKmh; }
    public boolean isGeofenceEnabled() { return geofenceEnabled; }
    public void setGeofenceEnabled(boolean geofenceEnabled) { this.geofenceEnabled = geofenceEnabled; }
    public Double getGeofenceCenterLat() { return geofenceCenterLat; }
    public void setGeofenceCenterLat(Double geofenceCenterLat) { this.geofenceCenterLat = geofenceCenterLat; }
    public Double getGeofenceCenterLng() { return geofenceCenterLng; }
    public void setGeofenceCenterLng(Double geofenceCenterLng) { this.geofenceCenterLng = geofenceCenterLng; }
    public Double getGeofenceRadiusMeters() { return geofenceRadiusMeters; }
    public void setGeofenceRadiusMeters(Double geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; }
    public boolean isTowingAlertEnabled() { return towingAlertEnabled; }
    public void setTowingAlertEnabled(boolean towingAlertEnabled) { this.towingAlertEnabled = towingAlertEnabled; }
    public boolean isCrashAlertEnabled() { return crashAlertEnabled; }
    public void setCrashAlertEnabled(boolean crashAlertEnabled) { this.crashAlertEnabled = crashAlertEnabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
