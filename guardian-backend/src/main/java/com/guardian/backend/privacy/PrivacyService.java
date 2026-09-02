package com.guardian.backend.privacy;

import com.guardian.backend.driver.DriverProfileRepository;
import com.guardian.backend.guardian.GuardianAlertRepository;
import com.guardian.backend.reminder.ReminderRepository;
import com.guardian.backend.telemetry.TelemetrySnapshotRepository;
import com.guardian.backend.trip.Trip;
import com.guardian.backend.trip.TripRepository;
import com.guardian.backend.user.User;
import com.guardian.backend.user.UserRepository;
import com.guardian.backend.vehicle.Vehicle;
import com.guardian.backend.vehicle.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PrivacyService {

    private static final Logger log = LoggerFactory.getLogger(PrivacyService.class);

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final TelemetrySnapshotRepository telemetryRepository;
    private final GuardianAlertRepository alertRepository;
    private final ReminderRepository reminderRepository;
    private final DriverProfileRepository profileRepository;

    public PrivacyService(UserRepository userRepository,
                          VehicleRepository vehicleRepository,
                          TripRepository tripRepository,
                          TelemetrySnapshotRepository telemetryRepository,
                          GuardianAlertRepository alertRepository,
                          ReminderRepository reminderRepository,
                          DriverProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
        this.telemetryRepository = telemetryRepository;
        this.alertRepository = alertRepository;
        this.reminderRepository = reminderRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public Map<String, Object> purgeUserData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Vehicle> vehicles = vehicleRepository.findByOwnerId(userId);
        List<Trip> trips = tripRepository.findByDriverIdOrderByStartTimeDesc(userId);

        long deletedSnapshots = 0;
        for (Trip trip : trips) {
            deletedSnapshots += telemetryRepository.countByTripId(trip.getId());
            telemetryRepository.deleteByTripId(trip.getId());
        }

        long deletedAlerts = 0;
        for (Vehicle vehicle : vehicles) {
            deletedAlerts += alertRepository.findByVehicleIdOrderByTimestampDesc(vehicle.getId()).size();
            alertRepository.deleteByVehicleId(vehicle.getId());
        }

        int deletedReminders = reminderRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
        reminderRepository.deleteAll(reminderRepository.findByUserIdOrderByCreatedAtDesc(userId));

        int deletedTrips = trips.size();
        tripRepository.deleteAll(trips);

        log.info("Purged user {} privacy data: {} snapshots, {} trips, {} alerts, {} reminders",
                user.getEmail(), deletedSnapshots, deletedTrips, deletedAlerts, deletedReminders);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("message", "All telemetry, trip logs, alerts, and reminders permanently erased.");
        result.put("deletedSnapshots", deletedSnapshots);
        result.put("deletedTrips", deletedTrips);
        result.put("deletedAlerts", deletedAlerts);
        result.put("deletedReminders", deletedReminders);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPrivacySummary(UUID userId) {
        List<Vehicle> vehicles = vehicleRepository.findByOwnerId(userId);
        List<Trip> trips = tripRepository.findByDriverIdOrderByStartTimeDesc(userId);
        int reminderCount = reminderRepository.findByUserIdOrderByCreatedAtDesc(userId).size();

        Map<String, Object> summary = new HashMap<>();
        summary.put("registeredVehicles", vehicles.size());
        summary.put("recordedTrips", trips.size());
        summary.put("storedReminders", reminderCount);
        summary.put("dataRetentionPolicy", "DPDPA 2023 Compliant: Telemetry purges on request; In-trip conversation RAM purges on engine off.");
        return summary;
    }
}
