package com.guardian.backend.reminder.dto;

import com.guardian.backend.reminder.Reminder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateReminderRequest(
        UUID vehicleId,

        @NotBlank @Size(max = 255)
        String title,

        Reminder.ReminderType reminderType,

        Double dueOdometerKm,

        Instant dueDate,

        String notes
) {}
