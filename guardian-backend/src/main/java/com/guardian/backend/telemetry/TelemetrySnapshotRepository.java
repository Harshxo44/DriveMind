package com.guardian.backend.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetrySnapshotRepository extends JpaRepository<TelemetrySnapshot, UUID> {
    List<TelemetrySnapshot> findByTripIdOrderByTimestampAsc(UUID tripId);
    Optional<TelemetrySnapshot> findFirstByVehicleIdOrderByTimestampDesc(UUID vehicleId);
    List<TelemetrySnapshot> findTop50ByVehicleIdOrderByTimestampDesc(UUID vehicleId);
    long countByTripId(UUID tripId);
    void deleteByTripId(UUID tripId);
}
