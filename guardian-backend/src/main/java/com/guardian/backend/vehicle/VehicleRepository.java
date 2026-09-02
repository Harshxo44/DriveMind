package com.guardian.backend.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByOwnerId(UUID ownerId);

    Optional<Vehicle> findByVin(String vin);
}