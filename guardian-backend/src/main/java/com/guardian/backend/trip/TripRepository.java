package com.guardian.backend.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByVehicleIdOrderByStartTimeDesc(UUID vehicleId);
    List<Trip> findByDriverIdOrderByStartTimeDesc(UUID driverId);
    Optional<Trip> findFirstByVehicleIdAndStatusOrderByStartTimeDesc(UUID vehicleId, Trip.TripStatus status);
}
