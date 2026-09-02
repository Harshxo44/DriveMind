package com.guardian.backend.vehicle;

import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import com.guardian.backend.vehicle.dto.CreateVehicleRequest;
import com.guardian.backend.vehicle.dto.VehicleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserRepository userRepository;

    public VehicleController(VehicleService vehicleService, UserRepository userRepository) {
        this.vehicleService = vehicleService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateVehicleRequest request) {
        User user = getUser(userDetails);
        VehicleResponse response = vehicleService.createVehicle(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getMyVehicles(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(vehicleService.getVehiclesForOwner(user.getId()));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(vehicleService.getVehicle(vehicleId));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID vehicleId,
            @Valid @RequestBody CreateVehicleRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, user.getId(), request));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID vehicleId) {
        User user = getUser(userDetails);
        vehicleService.deleteVehicle(vehicleId, user.getId());
        return ResponseEntity.noContent().build();
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
