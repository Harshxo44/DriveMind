package com.velora.backend.guardian;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuardianConfigRepository extends JpaRepository<GuardianConfig, UUID> {
    Optional<GuardianConfig> findByVehicleId(UUID vehicleId);
}
