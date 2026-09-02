package com.guardian.backend.guardian;

import com.guardian.backend.exception.ResourceNotFoundException;
import com.guardian.backend.guardian.dto.CreateAlertRequest;
import com.guardian.backend.guardian.dto.GuardianAlertResponse;
import com.guardian.backend.guardian.dto.GuardianConfigResponse;
import com.guardian.backend.guardian.dto.UpdateGuardianConfigRequest;
import com.guardian.backend.vehicle.Vehicle;
import com.guardian.backend.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GuardianService {

    private final GuardianConfigRepository configRepository;
    private final GuardianAlertRepository alertRepository;
    private final VehicleRepository vehicleRepository;

    public GuardianService(GuardianConfigRepository configRepository, GuardianAlertRepository alertRepository, VehicleRepository vehicleRepository) {
        this.configRepository = configRepository;
        this.alertRepository = alertRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public GuardianConfigResponse getOrCreateConfig(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        GuardianConfig config = configRepository.findByVehicleId(vehicleId)
                .orElseGet(() -> configRepository.save(GuardianConfig.builder().vehicle(vehicle).build()));

        return toConfigResponse(config);
    }

    @Transactional
    public GuardianConfigResponse updateConfig(UUID vehicleId, UpdateGuardianConfigRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        GuardianConfig config = configRepository.findByVehicleId(vehicleId)
                .orElseGet(() -> GuardianConfig.builder().vehicle(vehicle).build());

        if (request.guardianModeEnabled() != null) config.setGuardianModeEnabled(request.guardianModeEnabled());
        if (request.valetModeEnabled() != null) config.setValetModeEnabled(request.valetModeEnabled());
        if (request.maxSpeedLimitKmh() != null) config.setMaxSpeedLimitKmh(request.maxSpeedLimitKmh());
        if (request.geofenceEnabled() != null) config.setGeofenceEnabled(request.geofenceEnabled());
        if (request.geofenceCenterLat() != null) config.setGeofenceCenterLat(request.geofenceCenterLat());
        if (request.geofenceCenterLng() != null) config.setGeofenceCenterLng(request.geofenceCenterLng());
        if (request.geofenceRadiusMeters() != null) config.setGeofenceRadiusMeters(request.geofenceRadiusMeters());
        if (request.towingAlertEnabled() != null) config.setTowingAlertEnabled(request.towingAlertEnabled());
        if (request.crashAlertEnabled() != null) config.setCrashAlertEnabled(request.crashAlertEnabled());

        config = configRepository.save(config);
        return toConfigResponse(config);
    }

    @Transactional
    public GuardianAlertResponse createAlert(CreateAlertRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.vehicleId()));

        GuardianAlert alert = GuardianAlert.builder()
                .vehicle(vehicle)
                .alertType(request.alertType())
                .severity(request.severity() != null ? request.severity() : GuardianAlert.AlertSeverity.MEDIUM)
                .title(request.title())
                .description(request.description())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        alert = alertRepository.save(alert);
        return toAlertResponse(alert);
    }

    @Transactional(readOnly = true)
    public List<GuardianAlertResponse> getAlerts(UUID vehicleId) {
        return alertRepository.findByVehicleIdOrderByTimestampDesc(vehicleId).stream()
                .map(this::toAlertResponse)
                .toList();
    }

    @Transactional
    public GuardianAlertResponse acknowledgeAlert(UUID alertId) {
        GuardianAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));
        alert.setAcknowledged(true);
        alert = alertRepository.save(alert);
        return toAlertResponse(alert);
    }

    private GuardianConfigResponse toConfigResponse(GuardianConfig config) {
        return new GuardianConfigResponse(
                config.getId(),
                config.getVehicle().getId(),
                config.isGuardianModeEnabled(),
                config.isValetModeEnabled(),
                config.getMaxSpeedLimitKmh(),
                config.isGeofenceEnabled(),
                config.getGeofenceCenterLat(),
                config.getGeofenceCenterLng(),
                config.getGeofenceRadiusMeters(),
                config.isTowingAlertEnabled(),
                config.isCrashAlertEnabled(),
                config.getUpdatedAt()
        );
    }

    private GuardianAlertResponse toAlertResponse(GuardianAlert alert) {
        return new GuardianAlertResponse(
                alert.getId(),
                alert.getVehicle().getId(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.isAcknowledged(),
                alert.getTimestamp()
        );
    }
}
