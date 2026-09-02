package com.guardian.backend.driver;

import com.guardian.backend.driver.dto.DriverProfileResponse;
import com.guardian.backend.driver.dto.UpdateDriverProfileRequest;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverProfileController {

    private final DriverProfileService profileService;
    private final UserRepository userRepository;

    public DriverProfileController(DriverProfileService profileService, UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<DriverProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(profileService.getOrCreateProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<DriverProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateDriverProfileRequest request) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        if (userDetails == null) {
            return userRepository.findAll().stream().findFirst()
                    .map(User::getId)
                    .orElse(UUID.randomUUID());
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().map(User::getId).orElse(UUID.randomUUID()));
    }
}
