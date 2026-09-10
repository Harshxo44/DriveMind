package com.velora.backend.guardian;

import com.velora.backend.vehicle.Vehicle;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "guardian_alerts")
public class GuardianAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity = AlertSeverity.MEDIUM;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double latitude;
    private Double longitude;

    private boolean acknowledged = false;

    @Column(updatable = false)
    private Instant timestamp = Instant.now();

    public GuardianAlert() {}

    public GuardianAlert(UUID id, Vehicle vehicle, AlertType alertType, AlertSeverity severity, String title, String description, Double latitude, Double longitude, boolean acknowledged, Instant timestamp) {
        this.id = id;
        this.vehicle = vehicle;
        this.alertType = alertType;
        this.severity = severity != null ? severity : AlertSeverity.MEDIUM;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.acknowledged = acknowledged;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static GuardianAlertBuilder builder() {
        return new GuardianAlertBuilder();
    }

    public static class GuardianAlertBuilder {
        private UUID id;
        private Vehicle vehicle;
        private AlertType alertType;
        private AlertSeverity severity = AlertSeverity.MEDIUM;
        private String title;
        private String description;
        private Double latitude;
        private Double longitude;
        private boolean acknowledged = false;
        private Instant timestamp = Instant.now();

        public GuardianAlertBuilder id(UUID id) { this.id = id; return this; }
        public GuardianAlertBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public GuardianAlertBuilder alertType(AlertType alertType) { this.alertType = alertType; return this; }
        public GuardianAlertBuilder severity(AlertSeverity severity) { this.severity = severity; return this; }
        public GuardianAlertBuilder title(String title) { this.title = title; return this; }
        public GuardianAlertBuilder description(String description) { this.description = description; return this; }
        public GuardianAlertBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public GuardianAlertBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public GuardianAlertBuilder acknowledged(boolean acknowledged) { this.acknowledged = acknowledged; return this; }
        public GuardianAlertBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public GuardianAlert build() {
            return new GuardianAlert(id, vehicle, alertType, severity, title, description, latitude, longitude, acknowledged, timestamp);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public enum AlertType {
        SPEED_VIOLATION,
        GEOFENCE_BREACH,
        TOWING_DETECTED,
        CRASH_DETECTED,
        VALET_VIOLATION,
        LOW_BATTERY,
        ENGINE_OVERHEAT,
        DTC_FAULT
    }

    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
