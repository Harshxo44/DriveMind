package com.velora.backend.guardian;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GuardianAlertRepository extends JpaRepository<GuardianAlert, UUID> {
    List<GuardianAlert> findByVehicleIdOrderByTimestampDesc(UUID vehicleId);
    List<GuardianAlert> findByVehicleIdAndAcknowledgedFalseOrderByTimestampDesc(UUID vehicleId);
    long countByVehicleIdAndAcknowledgedFalse(UUID vehicleId);
    void deleteByVehicleId(UUID vehicleId);
}
