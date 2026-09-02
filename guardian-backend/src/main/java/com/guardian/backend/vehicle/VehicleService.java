package com.guardian.backend.vehicle;

import com.guardian.backend.exception.BadRequestException;
import com.guardian.backend.exception.ResourceNotFoundException;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import com.guardian.backend.vehicle.dto.CreateVehicleRequest;
import com.guardian.backend.vehicle.dto.VehicleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public VehicleResponse createVehicle(UUID ownerId, CreateVehicleRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        if (request.vin() != null && vehicleRepository.findByVin(request.vin()).isPresent()) {
            throw new BadRequestException("Vehicle with VIN " + request.vin() + " already registered");
        }

        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .make(request.make())
                .model(request.model())
                .year(request.year())
                .vin(request.vin())
                .registrationNumber(request.registrationNumber())
                .obdProtocol(request.obdProtocol())
                .build();

        vehicle = vehicleRepository.save(vehicle);
        return toResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesForOwner(UUID ownerId) {
        return vehicleRepository.findByOwnerId(ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));
        return toResponse(vehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID vehicleId, UUID ownerId, CreateVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        if (!vehicle.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You do not own this vehicle");
        }

        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setVin(request.vin());
        vehicle.setRegistrationNumber(request.registrationNumber());
        vehicle.setObdProtocol(request.obdProtocol());

        vehicle = vehicleRepository.save(vehicle);
        return toResponse(vehicle);
    }

    @Transactional
    public void deleteVehicle(UUID vehicleId, UUID ownerId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        if (!vehicle.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You do not own this vehicle");
        }

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getOwner().getId(),
                vehicle.getOwner().getName(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getVin(),
                vehicle.getRegistrationNumber(),
                vehicle.getObdProtocol(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
