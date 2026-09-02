package com.guardian.backend.reminder;

import com.guardian.backend.reminder.dto.CreateReminderRequest;
import com.guardian.backend.reminder.dto.ReminderResponse;
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
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final ReminderService reminderService;
    private final UserRepository userRepository;

    public ReminderController(ReminderService reminderService, UserRepository userRepository) {
        this.reminderService = reminderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReminderRequest request) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.createReminder(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getReminders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "false") boolean pendingOnly) {
        UUID userId = resolveUserId(userDetails);
        if (pendingOnly) {
            return ResponseEntity.ok(reminderService.getPendingRemindersForUser(userId));
        }
        return ResponseEntity.ok(reminderService.getRemindersForUser(userId));
    }

    @PutMapping("/{reminderId}/complete")
    public ResponseEntity<ReminderResponse> markCompleted(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID reminderId) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.ok(reminderService.markCompleted(reminderId, userId));
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID reminderId) {
        UUID userId = resolveUserId(userDetails);
        reminderService.deleteReminder(reminderId, userId);
        return ResponseEntity.noContent().build();
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
