package com.guardian.backend.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Optional<Device> findBySerialNumber(String serialNumber);

    List<Device> findByUserId(UUID userId);

    boolean existsBySerialNumber(String serialNumber);
}