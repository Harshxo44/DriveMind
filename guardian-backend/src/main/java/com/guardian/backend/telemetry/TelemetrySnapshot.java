package com.guardian.backend.telemetry;

import com.guardian.backend.trip.Trip;
import com.guardian.backend.vehicle.Vehicle;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "telemetry_snapshots")
public class TelemetrySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private Double speedKmh;
    private Integer rpm;
    private Double engineCoolantTempC;
    private Double throttlePositionPct;
    private Double fuelLevelPct;
    private Double batteryVoltageV;
    private Double latitude;
    private Double longitude;
    private Double heading;

    @Column(length = 255)
    private String activeDtcCodes;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public TelemetrySnapshot() {}

    public TelemetrySnapshot(UUID id, Vehicle vehicle, Trip trip, Double speedKmh, Integer rpm, Double engineCoolantTempC, Double throttlePositionPct, Double fuelLevelPct, Double batteryVoltageV, Double latitude, Double longitude, Double heading, String activeDtcCodes, Instant timestamp) {
        this.id = id;
        this.vehicle = vehicle;
        this.trip = trip;
        this.speedKmh = speedKmh;
        this.rpm = rpm;
        this.engineCoolantTempC = engineCoolantTempC;
        this.throttlePositionPct = throttlePositionPct;
        this.fuelLevelPct = fuelLevelPct;
        this.batteryVoltageV = batteryVoltageV;
        this.latitude = latitude;
        this.longitude = longitude;
        this.heading = heading;
        this.activeDtcCodes = activeDtcCodes;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static TelemetrySnapshotBuilder builder() {
        return new TelemetrySnapshotBuilder();
    }

    public static class TelemetrySnapshotBuilder {
        private UUID id;
        private Vehicle vehicle;
        private Trip trip;
        private Double speedKmh;
        private Integer rpm;
        private Double engineCoolantTempC;
        private Double throttlePositionPct;
        private Double fuelLevelPct;
        private Double batteryVoltageV;
        private Double latitude;
        private Double longitude;
        private Double heading;
        private String activeDtcCodes;
        private Instant timestamp = Instant.now();

        public TelemetrySnapshotBuilder id(UUID id) { this.id = id; return this; }
        public TelemetrySnapshotBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public TelemetrySnapshotBuilder trip(Trip trip) { this.trip = trip; return this; }
        public TelemetrySnapshotBuilder speedKmh(Double speedKmh) { this.speedKmh = speedKmh; return this; }
        public TelemetrySnapshotBuilder rpm(Integer rpm) { this.rpm = rpm; return this; }
        public TelemetrySnapshotBuilder engineCoolantTempC(Double engineCoolantTempC) { this.engineCoolantTempC = engineCoolantTempC; return this; }
        public TelemetrySnapshotBuilder throttlePositionPct(Double throttlePositionPct) { this.throttlePositionPct = throttlePositionPct; return this; }
        public TelemetrySnapshotBuilder fuelLevelPct(Double fuelLevelPct) { this.fuelLevelPct = fuelLevelPct; return this; }
        public TelemetrySnapshotBuilder batteryVoltageV(Double batteryVoltageV) { this.batteryVoltageV = batteryVoltageV; return this; }
        public TelemetrySnapshotBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public TelemetrySnapshotBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public TelemetrySnapshotBuilder heading(Double heading) { this.heading = heading; return this; }
        public TelemetrySnapshotBuilder activeDtcCodes(String activeDtcCodes) { this.activeDtcCodes = activeDtcCodes; return this; }
        public TelemetrySnapshotBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public TelemetrySnapshot build() {
            return new TelemetrySnapshot(id, vehicle, trip, speedKmh, rpm, engineCoolantTempC, throttlePositionPct, fuelLevelPct, batteryVoltageV, latitude, longitude, heading, activeDtcCodes, timestamp);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }
    public Double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }
    public Integer getRpm() { return rpm; }
    public void setRpm(Integer rpm) { this.rpm = rpm; }
    public Double getEngineCoolantTempC() { return engineCoolantTempC; }
    public void setEngineCoolantTempC(Double engineCoolantTempC) { this.engineCoolantTempC = engineCoolantTempC; }
    public Double getThrottlePositionPct() { return throttlePositionPct; }
    public void setThrottlePositionPct(Double throttlePositionPct) { this.throttlePositionPct = throttlePositionPct; }
    public Double getFuelLevelPct() { return fuelLevelPct; }
    public void setFuelLevelPct(Double fuelLevelPct) { this.fuelLevelPct = fuelLevelPct; }
    public Double getBatteryVoltageV() { return batteryVoltageV; }
    public void setBatteryVoltageV(Double batteryVoltageV) { this.batteryVoltageV = batteryVoltageV; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    public String getActiveDtcCodes() { return activeDtcCodes; }
    public void setActiveDtcCodes(String activeDtcCodes) { this.activeDtcCodes = activeDtcCodes; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
