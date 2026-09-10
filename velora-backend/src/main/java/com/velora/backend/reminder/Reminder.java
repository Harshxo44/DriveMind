package com.velora.backend.reminder;

import com.velora.backend.user.User;
import com.velora.backend.vehicle.Vehicle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reminders")
public class Reminder {

    public enum ReminderType {
        MAINTENANCE, SAFETY, CONTEXTUAL, USER_NOTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType reminderType = ReminderType.MAINTENANCE;

    private Double dueOdometerKm;

    private Instant dueDate;

    private boolean completed = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Reminder() {}

    public Reminder(UUID id, User user, Vehicle vehicle, String title, ReminderType reminderType,
                    Double dueOdometerKm, Instant dueDate, boolean completed, String notes,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.vehicle = vehicle;
        this.title = title;
        this.reminderType = reminderType != null ? reminderType : ReminderType.MAINTENANCE;
        this.dueOdometerKm = dueOdometerKm;
        this.dueDate = dueDate;
        this.completed = completed;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static ReminderBuilder builder() {
        return new ReminderBuilder();
    }

    public static class ReminderBuilder {
        private UUID id;
        private User user;
        private Vehicle vehicle;
        private String title;
        private ReminderType reminderType = ReminderType.MAINTENANCE;
        private Double dueOdometerKm;
        private Instant dueDate;
        private boolean completed = false;
        private String notes;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public ReminderBuilder id(UUID id) { this.id = id; return this; }
        public ReminderBuilder user(User user) { this.user = user; return this; }
        public ReminderBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public ReminderBuilder title(String title) { this.title = title; return this; }
        public ReminderBuilder reminderType(ReminderType reminderType) { this.reminderType = reminderType; return this; }
        public ReminderBuilder dueOdometerKm(Double dueOdometerKm) { this.dueOdometerKm = dueOdometerKm; return this; }
        public ReminderBuilder dueDate(Instant dueDate) { this.dueDate = dueDate; return this; }
        public ReminderBuilder completed(boolean completed) { this.completed = completed; return this; }
        public ReminderBuilder notes(String notes) { this.notes = notes; return this; }
        public ReminderBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public ReminderBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Reminder build() {
            return new Reminder(id, user, vehicle, title, reminderType, dueOdometerKm, dueDate, completed, notes, createdAt, updatedAt);
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }
    public Double getDueOdometerKm() { return dueOdometerKm; }
    public void setDueOdometerKm(Double dueOdometerKm) { this.dueOdometerKm = dueOdometerKm; }
    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
