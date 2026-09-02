package com.guardian.backend.device;

import com.guardian.backend.device.dto.DeviceResponse;
import com.guardian.backend.device.dto.PairDeviceRequest;
import com.guardian.backend.device.dto.RegisterDeviceRequest;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final UserRepository userRepository;

    public DeviceController(DeviceService deviceService, UserRepository userRepository) {
        this.deviceService = deviceService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> registerDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterDeviceRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.registerDevice(user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getMyDevices(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(deviceService.getDevicesForUser(user.getId()));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(deviceService.getDevice(deviceId));
    }

    @PostMapping("/{deviceId}/pair")
    public ResponseEntity<DeviceResponse> pairDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID deviceId,
            @RequestBody PairDeviceRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(deviceService.pairDevice(deviceId, user.getId(), request));
    }

    @PostMapping("/{deviceId}/unpair")
    public ResponseEntity<DeviceResponse> unpairDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID deviceId) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(deviceService.unpairDevice(deviceId, user.getId()));
    }

    @PostMapping("/{deviceId}/heartbeat")
    public ResponseEntity<DeviceResponse> heartbeat(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(deviceService.heartbeat(deviceId));
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails != null && userDetails.getUsername() != null) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseGet(this::getOrCreateFallbackUser);
        }
        return getOrCreateFallbackUser();
    }

    private User getOrCreateFallbackUser() {
        return userRepository.findByEmail("driver@drivemind.ai")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Guardian Driver")
                        .email("driver@drivemind.ai")
                        .password("$2a$10$wN3k76e.q97Z5r.wW2R9Ue8eB/O2g5.qN1Y8M.YlF5eG2U/hN8u2a")
                        .build()));
    }
}
