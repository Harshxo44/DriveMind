package com.guardian.backend.reminder.dto;

import com.guardian.backend.reminder.Reminder;

import java.time.Instant;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        UUID userId,
        UUID vehicleId,
        String title,
        Reminder.ReminderType reminderType,
        Double dueOdometerKm,
        Instant dueDate,
        boolean completed,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReminderResponse fromEntity(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getUser().getId(),
                reminder.getVehicle() != null ? reminder.getVehicle().getId() : null,
                reminder.getTitle(),
                reminder.getReminderType(),
                reminder.getDueOdometerKm(),
                reminder.getDueDate(),
                reminder.isCompleted(),
                reminder.getNotes(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt()
        );
    }
}
