package com.velora.backend.reminder;

import com.velora.backend.exception.ResourceNotFoundException;
import com.velora.backend.reminder.dto.CreateReminderRequest;
import com.velora.backend.reminder.dto.ReminderResponse;
import com.velora.backend.user.User;
import com.velora.backend.user.UserRepository;
import com.velora.backend.vehicle.Vehicle;
import com.velora.backend.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public ReminderService(ReminderRepository reminderRepository, UserRepository userRepository, VehicleRepository vehicleRepository) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public ReminderResponse createReminder(UUID userId, CreateReminderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Vehicle vehicle = null;
        if (request.vehicleId() != null) {
            vehicle = vehicleRepository.findById(request.vehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        }

        Reminder reminder = Reminder.builder()
                .user(user)
                .vehicle(vehicle)
                .title(request.title())
                .reminderType(request.reminderType() != null ? request.reminderType() : Reminder.ReminderType.MAINTENANCE)
                .dueOdometerKm(request.dueOdometerKm())
                .dueDate(request.dueDate())
                .notes(request.notes())
                .completed(false)
                .build();

        Reminder saved = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersForUser(UUID userId) {
        return reminderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReminderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getPendingRemindersForUser(UUID userId) {
        return reminderRepository.findByUserIdAndCompletedFalseOrderByDueDateAsc(userId).stream()
                .map(ReminderResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ReminderResponse markCompleted(UUID reminderId, UUID userId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Reminder not owned by user: " + reminderId);
        }

        reminder.setCompleted(true);
        Reminder saved = reminderRepository.save(reminder);
        return ReminderResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteReminder(UUID reminderId, UUID userId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + reminderId));

        if (!reminder.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Reminder not owned by user: " + reminderId);
        }

        reminderRepository.delete(reminder);
    }
}
