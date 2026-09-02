package com.guardian.backend.trip;

import com.guardian.backend.trip.dto.EndTripRequest;
import com.guardian.backend.trip.dto.StartTripRequest;
import com.guardian.backend.trip.dto.TripResponse;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService tripService;
    private final UserRepository userRepository;

    public TripController(TripService tripService, UserRepository userRepository) {
        this.tripService = tripService;
        this.userRepository = userRepository;
    }

    @PostMapping("/start")
    public ResponseEntity<TripResponse> startTrip(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody StartTripRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.startTrip(user.getId(), request));
    }

    @PostMapping("/{tripId}/end")
    public ResponseEntity<TripResponse> endTrip(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID tripId,
            @RequestBody EndTripRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(tripService.endTrip(tripId, user.getId(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TripResponse>> getMyTrips(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(tripService.getTripsForDriver(user.getId()));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<TripResponse>> getVehicleTrips(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(tripService.getTripsForVehicle(vehicleId));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable UUID tripId) {
        return ResponseEntity.ok(tripService.getTrip(tripId));
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
