package com.guardian.backend.telemetry;

import com.guardian.backend.exception.ResourceNotFoundException;
import com.guardian.backend.telemetry.dto.IngestTelemetryRequest;
import com.guardian.backend.telemetry.dto.TelemetryResponse;
import com.guardian.backend.trip.Trip;
import com.guardian.backend.trip.TripRepository;
import com.guardian.backend.vehicle.Vehicle;
import com.guardian.backend.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TelemetryService {

    private final TelemetrySnapshotRepository telemetryRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    public TelemetryService(TelemetrySnapshotRepository telemetryRepository, VehicleRepository vehicleRepository, TripRepository tripRepository) {
        this.telemetryRepository = telemetryRepository;
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional
    public TelemetryResponse ingest(IngestTelemetryRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.vehicleId()));

        Trip trip = null;
        if (request.tripId() != null) {
            trip = tripRepository.findById(request.tripId()).orElse(null);
        } else {
            // Auto-associate active trip if one exists
            trip = tripRepository.findFirstByVehicleIdAndStatusOrderByStartTimeDesc(
                    vehicle.getId(), Trip.TripStatus.IN_PROGRESS).orElse(null);
        }

        TelemetrySnapshot snapshot = TelemetrySnapshot.builder()
                .vehicle(vehicle)
                .trip(trip)
                .speedKmh(request.speedKmh())
                .rpm(request.rpm())
                .engineCoolantTempC(request.engineCoolantTempC())
                .throttlePositionPct(request.throttlePositionPct())
                .fuelLevelPct(request.fuelLevelPct())
                .batteryVoltageV(request.batteryVoltageV())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .heading(request.heading())
                .activeDtcCodes(request.activeDtcCodes())
                .timestamp(Instant.now())
                .build();

        snapshot = telemetryRepository.save(snapshot);
        return toResponse(snapshot);
    }

    @Transactional(readOnly = true)
    public TelemetryResponse getLatestForVehicle(UUID vehicleId) {
        return telemetryRepository.findFirstByVehicleIdOrderByTimestampDesc(vehicleId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TelemetryResponse> getRecentForVehicle(UUID vehicleId) {
        return telemetryRepository.findTop50ByVehicleIdOrderByTimestampDesc(vehicleId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TelemetryResponse> getForTrip(UUID tripId) {
        return telemetryRepository.findByTripIdOrderByTimestampAsc(tripId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TelemetryResponse toResponse(TelemetrySnapshot snapshot) {
        return new TelemetryResponse(
                snapshot.getId(),
                snapshot.getVehicle().getId(),
                snapshot.getTrip() != null ? snapshot.getTrip().getId() : null,
                snapshot.getSpeedKmh(),
                snapshot.getRpm(),
                snapshot.getEngineCoolantTempC(),
                snapshot.getThrottlePositionPct(),
                snapshot.getFuelLevelPct(),
                snapshot.getBatteryVoltageV(),
                snapshot.getLatitude(),
                snapshot.getLongitude(),
                snapshot.getHeading(),
                snapshot.getActiveDtcCodes(),
                snapshot.getTimestamp()
        );
    }
}
