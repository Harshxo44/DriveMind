package com.velora.backend.vehicle;

import com.velora.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotBlank
    @Size(max = 50)
    private String make;

    @NotBlank
    @Size(max = 50)
    private String model;

    @Column(name = "manufacture_year", length = 4)
    private String year;

    @Size(max = 17)
    @Column(unique = true)
    private String vin;

    @Size(max = 20)
    private String registrationNumber;

    @Size(max = 30)
    private String obdProtocol;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Vehicle() {}

    public Vehicle(UUID id, User owner, String make, String model, String year, String vin, String registrationNumber, String obdProtocol, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.owner = owner;
        this.make = make;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.registrationNumber = registrationNumber;
        this.obdProtocol = obdProtocol;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static VehicleBuilder builder() {
        return new VehicleBuilder();
    }

    public static class VehicleBuilder {
        private UUID id;
        private User owner;
        private String make;
        private String model;
        private String year;
        private String vin;
        private String registrationNumber;
        private String obdProtocol;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public VehicleBuilder id(UUID id) { this.id = id; return this; }
        public VehicleBuilder owner(User owner) { this.owner = owner; return this; }
        public VehicleBuilder make(String make) { this.make = make; return this; }
        public VehicleBuilder model(String model) { this.model = model; return this; }
        public VehicleBuilder year(String year) { this.year = year; return this; }
        public VehicleBuilder vin(String vin) { this.vin = vin; return this; }
        public VehicleBuilder registrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; return this; }
        public VehicleBuilder obdProtocol(String obdProtocol) { this.obdProtocol = obdProtocol; return this; }
        public VehicleBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public VehicleBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Vehicle build() {
            return new Vehicle(id, owner, make, model, year, vin, registrationNumber, obdProtocol, createdAt, updatedAt);
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getObdProtocol() { return obdProtocol; }
    public void setObdProtocol(String obdProtocol) { this.obdProtocol = obdProtocol; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
