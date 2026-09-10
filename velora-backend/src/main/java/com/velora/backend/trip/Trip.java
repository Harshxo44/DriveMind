package com.velora.backend.trip;

import com.velora.backend.user.User;
import com.velora.backend.vehicle.Vehicle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @NotNull
    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    private Double distanceKm = 0.0;
    private Double avgSpeedKmh = 0.0;
    private Double maxSpeedKmh = 0.0;
    private Double fuelConsumedLiters = 0.0;
    private Integer harshBrakingCount = 0;
    private Integer harshAccelerationCount = 0;
    private Integer safetyScore = 100;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.IN_PROGRESS;

    private String startLocation;
    private String endLocation;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Trip() {}

    public Trip(UUID id, Vehicle vehicle, User driver, Instant startTime, Instant endTime, Double distanceKm, Double avgSpeedKmh, Double maxSpeedKmh, Double fuelConsumedLiters, Integer harshBrakingCount, Integer harshAccelerationCount, Integer safetyScore, TripStatus status, String startLocation, String endLocation, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.vehicle = vehicle;
        this.driver = driver;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distanceKm = distanceKm != null ? distanceKm : 0.0;
        this.avgSpeedKmh = avgSpeedKmh != null ? avgSpeedKmh : 0.0;
        this.maxSpeedKmh = maxSpeedKmh != null ? maxSpeedKmh : 0.0;
        this.fuelConsumedLiters = fuelConsumedLiters != null ? fuelConsumedLiters : 0.0;
        this.harshBrakingCount = harshBrakingCount != null ? harshBrakingCount : 0;
        this.harshAccelerationCount = harshAccelerationCount != null ? harshAccelerationCount : 0;
        this.safetyScore = safetyScore != null ? safetyScore : 100;
        this.status = status != null ? status : TripStatus.IN_PROGRESS;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static TripBuilder builder() {
        return new TripBuilder();
    }

    public static class TripBuilder {
        private UUID id;
        private Vehicle vehicle;
        private User driver;
        private Instant startTime;
        private Instant endTime;
        private Double distanceKm = 0.0;
        private Double avgSpeedKmh = 0.0;
        private Double maxSpeedKmh = 0.0;
        private Double fuelConsumedLiters = 0.0;
        private Integer harshBrakingCount = 0;
        private Integer harshAccelerationCount = 0;
        private Integer safetyScore = 100;
        private TripStatus status = TripStatus.IN_PROGRESS;
        private String startLocation;
        private String endLocation;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public TripBuilder id(UUID id) { this.id = id; return this; }
        public TripBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public TripBuilder driver(User driver) { this.driver = driver; return this; }
        public TripBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
        public TripBuilder endTime(Instant endTime) { this.endTime = endTime; return this; }
        public TripBuilder distanceKm(Double distanceKm) { this.distanceKm = distanceKm; return this; }
        public TripBuilder avgSpeedKmh(Double avgSpeedKmh) { this.avgSpeedKmh = avgSpeedKmh; return this; }
        public TripBuilder maxSpeedKmh(Double maxSpeedKmh) { this.maxSpeedKmh = maxSpeedKmh; return this; }
        public TripBuilder fuelConsumedLiters(Double fuelConsumedLiters) { this.fuelConsumedLiters = fuelConsumedLiters; return this; }
        public TripBuilder harshBrakingCount(Integer harshBrakingCount) { this.harshBrakingCount = harshBrakingCount; return this; }
        public TripBuilder harshAccelerationCount(Integer harshAccelerationCount) { this.harshAccelerationCount = harshAccelerationCount; return this; }
        public TripBuilder safetyScore(Integer safetyScore) { this.safetyScore = safetyScore; return this; }
        public TripBuilder status(TripStatus status) { this.status = status; return this; }
        public TripBuilder startLocation(String startLocation) { this.startLocation = startLocation; return this; }
        public TripBuilder endLocation(String endLocation) { this.endLocation = endLocation; return this; }
        public TripBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public TripBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Trip build() {
            return new Trip(id, vehicle, driver, startTime, endTime, distanceKm, avgSpeedKmh, maxSpeedKmh, fuelConsumedLiters, harshBrakingCount, harshAccelerationCount, safetyScore, status, startLocation, endLocation, createdAt, updatedAt);
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
    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getAvgSpeedKmh() { return avgSpeedKmh; }
    public void setAvgSpeedKmh(Double avgSpeedKmh) { this.avgSpeedKmh = avgSpeedKmh; }
    public Double getMaxSpeedKmh() { return maxSpeedKmh; }
    public void setMaxSpeedKmh(Double maxSpeedKmh) { this.maxSpeedKmh = maxSpeedKmh; }
    public Double getFuelConsumedLiters() { return fuelConsumedLiters; }
    public void setFuelConsumedLiters(Double fuelConsumedLiters) { this.fuelConsumedLiters = fuelConsumedLiters; }
    public Integer getHarshBrakingCount() { return harshBrakingCount; }
    public void setHarshBrakingCount(Integer harshBrakingCount) { this.harshBrakingCount = harshBrakingCount; }
    public Integer getHarshAccelerationCount() { return harshAccelerationCount; }
    public void setHarshAccelerationCount(Integer harshAccelerationCount) { this.harshAccelerationCount = harshAccelerationCount; }
    public Integer getSafetyScore() { return safetyScore; }
    public void setSafetyScore(Integer safetyScore) { this.safetyScore = safetyScore; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public enum TripStatus {
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
