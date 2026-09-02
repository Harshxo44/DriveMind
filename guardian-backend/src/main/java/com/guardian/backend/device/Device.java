package com.guardian.backend.device;

import com.guardian.backend.user.User;
import com.guardian.backend.vehicle.Vehicle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Size(max = 30)
    private String firmwareVersion;

    @Size(max = 30)
    private String hardwareVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.UNPAIRED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private Instant lastSeenAt;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Device() {}

    public Device(UUID id, String serialNumber, String firmwareVersion, String hardwareVersion, DeviceStatus status, User user, Vehicle vehicle, Instant lastSeenAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.firmwareVersion = firmwareVersion;
        this.hardwareVersion = hardwareVersion;
        this.status = status != null ? status : DeviceStatus.UNPAIRED;
        this.user = user;
        this.vehicle = vehicle;
        this.lastSeenAt = lastSeenAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static DeviceBuilder builder() {
        return new DeviceBuilder();
    }

    public static class DeviceBuilder {
        private UUID id;
        private String serialNumber;
        private String firmwareVersion;
        private String hardwareVersion;
        private DeviceStatus status = DeviceStatus.UNPAIRED;
        private User user;
        private Vehicle vehicle;
        private Instant lastSeenAt;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public DeviceBuilder id(UUID id) { this.id = id; return this; }
        public DeviceBuilder serialNumber(String serialNumber) { this.serialNumber = serialNumber; return this; }
        public DeviceBuilder firmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; return this; }
        public DeviceBuilder hardwareVersion(String hardwareVersion) { this.hardwareVersion = hardwareVersion; return this; }
        public DeviceBuilder status(DeviceStatus status) { this.status = status; return this; }
        public DeviceBuilder user(User user) { this.user = user; return this; }
        public DeviceBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public DeviceBuilder lastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; return this; }
        public DeviceBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public DeviceBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Device build() {
            return new Device(id, serialNumber, firmwareVersion, hardwareVersion, status, user, vehicle, lastSeenAt, createdAt, updatedAt);
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public String getHardwareVersion() { return hardwareVersion; }
    public void setHardwareVersion(String hardwareVersion) { this.hardwareVersion = hardwareVersion; }
    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public enum DeviceStatus {
        UNPAIRED,
        PAIRED,
        ACTIVE,
        SUSPENDED
    }
}
