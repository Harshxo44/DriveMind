package com.velora.backend.device;

import com.velora.backend.device.dto.DeviceResponse;
import com.velora.backend.device.dto.PairDeviceRequest;
import com.velora.backend.device.dto.RegisterDeviceRequest;
import com.velora.backend.exception.BadRequestException;
import com.velora.backend.exception.ResourceNotFoundException;
import com.velora.backend.user.User;
import com.velora.backend.user.UserRepository;
import com.velora.backend.vehicle.Vehicle;
import com.velora.backend.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository, VehicleRepository vehicleRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public DeviceResponse registerDevice(UUID userId, RegisterDeviceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (deviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new BadRequestException("Device with serial " + request.serialNumber() + " already registered");
        }

        Device device = Device.builder()
                .serialNumber(request.serialNumber())
                .firmwareVersion(request.firmwareVersion())
                .hardwareVersion(request.hardwareVersion())
                .user(user)
                .status(Device.DeviceStatus.UNPAIRED)
                .build();

        device = deviceRepository.save(device);
        return toResponse(device);
    }

    @Transactional
    public DeviceResponse pairDevice(UUID deviceId, UUID userId, PairDeviceRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (!device.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not own this device");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.vehicleId()));

        device.setVehicle(vehicle);
        device.setStatus(Device.DeviceStatus.PAIRED);
        device.setLastSeenAt(Instant.now());

        device = deviceRepository.save(device);
        return toResponse(device);
    }

    @Transactional
    public DeviceResponse unpairDevice(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (!device.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not own this device");
        }

        device.setVehicle(null);
        device.setStatus(Device.DeviceStatus.UNPAIRED);

        device = deviceRepository.save(device);
        return toResponse(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesForUser(UUID userId) {
        return deviceRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDevice(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        return toResponse(device);
    }

    @Transactional
    public DeviceResponse heartbeat(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        device.setLastSeenAt(Instant.now());
        if (device.getStatus() == Device.DeviceStatus.PAIRED) {
            device.setStatus(Device.DeviceStatus.ACTIVE);
        }
        device = deviceRepository.save(device);
        return toResponse(device);
    }

    private DeviceResponse toResponse(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getSerialNumber(),
                device.getFirmwareVersion(),
                device.getHardwareVersion(),
                device.getStatus(),
                device.getUser() != null ? device.getUser().getId() : null,
                device.getVehicle() != null ? device.getVehicle().getId() : null,
                device.getVehicle() != null ? device.getVehicle().getMake() : null,
                device.getVehicle() != null ? device.getVehicle().getModel() : null,
                device.getLastSeenAt(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }
}
