package com.guardian.backend.trip;

import com.guardian.backend.exception.BadRequestException;
import com.guardian.backend.exception.ResourceNotFoundException;
import com.guardian.backend.trip.dto.EndTripRequest;
import com.guardian.backend.trip.dto.StartTripRequest;
import com.guardian.backend.trip.dto.TripResponse;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import com.guardian.backend.vehicle.Vehicle;
import com.guardian.backend.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public TripService(TripRepository tripRepository, VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TripResponse startTrip(UUID driverId, StartTripRequest request) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", driverId));

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.vehicleId()));

        // Check if there is already an active trip for this vehicle
        tripRepository.findFirstByVehicleIdAndStatusOrderByStartTimeDesc(vehicle.getId(), Trip.TripStatus.IN_PROGRESS)
                .ifPresent(trip -> {
                    trip.setStatus(Trip.TripStatus.COMPLETED);
                    trip.setEndTime(Instant.now());
                    tripRepository.save(trip);
                });

        Trip trip = Trip.builder()
                .vehicle(vehicle)
                .driver(driver)
                .startTime(Instant.now())
                .startLocation(request.startLocation() != null ? request.startLocation() : "Origin")
                .status(Trip.TripStatus.IN_PROGRESS)
                .build();

        trip = tripRepository.save(trip);
        return toResponse(trip);
    }

    @Transactional
    public TripResponse endTrip(UUID tripId, UUID driverId, EndTripRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new BadRequestException("You are not the driver of this trip");
        }

        if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS) {
            throw new BadRequestException("Trip is already finalized: " + trip.getStatus());
        }

        trip.setEndTime(Instant.now());
        trip.setStatus(Trip.TripStatus.COMPLETED);
        if (request.endLocation() != null) trip.setEndLocation(request.endLocation());
        if (request.distanceKm() != null) trip.setDistanceKm(request.distanceKm());
        if (request.avgSpeedKmh() != null) trip.setAvgSpeedKmh(request.avgSpeedKmh());
        if (request.maxSpeedKmh() != null) trip.setMaxSpeedKmh(request.maxSpeedKmh());
        if (request.fuelConsumedLiters() != null) trip.setFuelConsumedLiters(request.fuelConsumedLiters());
        if (request.harshBrakingCount() != null) trip.setHarshBrakingCount(request.harshBrakingCount());
        if (request.harshAccelerationCount() != null) trip.setHarshAccelerationCount(request.harshAccelerationCount());

        // Calculate safety score (100 base, penalize for harsh braking/accel)
        int harshTotal = (request.harshBrakingCount() != null ? request.harshBrakingCount() : 0)
                + (request.harshAccelerationCount() != null ? request.harshAccelerationCount() : 0);
        int score = Math.max(20, 100 - (harshTotal * 5));
        trip.setSafetyScore(score);

        trip = tripRepository.save(trip);
        return toResponse(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getTripsForVehicle(UUID vehicleId) {
        return tripRepository.findByVehicleIdOrderByStartTimeDesc(vehicleId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getTripsForDriver(UUID driverId) {
        return tripRepository.findByDriverIdOrderByStartTimeDesc(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));
        return toResponse(trip);
    }

    public TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getVehicle().getId(),
                trip.getVehicle().getMake(),
                trip.getVehicle().getModel(),
                trip.getDriver().getId(),
                trip.getDriver().getName(),
                trip.getStartTime(),
                trip.getEndTime(),
                trip.getDistanceKm(),
                trip.getAvgSpeedKmh(),
                trip.getMaxSpeedKmh(),
                trip.getFuelConsumedLiters(),
                trip.getHarshBrakingCount(),
                trip.getHarshAccelerationCount(),
                trip.getSafetyScore(),
                trip.getStatus(),
                trip.getStartLocation(),
                trip.getEndLocation(),
                trip.getCreatedAt()
        );
    }
}
