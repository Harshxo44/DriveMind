package com.velora.backend.privacy;

import com.velora.backend.user.User;
import com.velora.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/privacy")
public class PrivacyController {

    private final PrivacyService privacyService;
    private final UserRepository userRepository;

    public PrivacyController(PrivacyService privacyService, UserRepository userRepository) {
        this.privacyService = privacyService;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPrivacySummary(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(privacyService.getPrivacySummary(userId));
    }

    @DeleteMapping("/purge-my-data")
    public ResponseEntity<Map<String, Object>> purgeUserData(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(privacyService.purgeUserData(userId));
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
