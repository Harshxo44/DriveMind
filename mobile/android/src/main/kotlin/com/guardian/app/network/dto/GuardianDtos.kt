package com.guardian.app.network.dto

import java.util.UUID

// 1. Auth DTOs
data class RegisterRequest(val name: String, val email: String, val phone: String?, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val accessToken: String, val tokenType: String, val userId: UUID, val email: String, val name: String, val role: String)

// 2. Driver Profile DTOs
data class DriverProfileResponse(
    val id: UUID,
    val userId: UUID,
    val displayName: String,
    val experienceLevel: String,
    val tonePreference: String,
    val speedAlertThresholdKmh: Double,
    val highRpmThreshold: Int,
    val voiceGreetingEnabled: Boolean
)

data class UpdateDriverProfileRequest(
    val displayName: String,
    val experienceLevel: String,
    val tonePreference: String,
    val speedAlertThresholdKmh: Double,
    val highRpmThreshold: Int,
    val voiceGreetingEnabled: Boolean
)

// 3. Vehicle DTOs
data class CreateVehicleRequest(val make: String, val model: String, val year: String?, val vin: String?, val registrationNumber: String?, val obdProtocol: String?)
data class VehicleResponse(val id: UUID, val make: String, val model: String, val year: String?, val vin: String?, val registrationNumber: String?, val obdProtocol: String?)

// 4. Device DTOs
data class RegisterDeviceRequest(val serialNumber: String, val hardwareVersion: String?, val firmwareVersion: String?)
data class PairDeviceRequest(val vehicleId: UUID)
data class DeviceResponse(val id: UUID, val serialNumber: String, val pairedVehicleId: UUID?, val status: String)

// 5. Trip DTOs
data class StartTripRequest(val vehicleId: UUID, val startLocation: String?)
data class EndTripRequest(
    val endLocation: String?,
    val distanceKm: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val fuelConsumedLiters: Double?,
    val harshBrakingCount: Int,
    val harshAccelerationCount: Int
)
data class TripResponse(
    val id: UUID,
    val vehicleId: UUID,
    val status: String,
    val startTime: String,
    val endTime: String?,
    val distanceKm: Double,
    val safetyScore: Int,
    val startLocation: String?,
    val endLocation: String?
)

// 6. Telemetry Ingest
data class IngestTelemetryRequest(
    val vehicleId: UUID,
    val tripId: UUID?,
    val speedKmh: Double,
    val rpm: Int,
    val engineCoolantTempC: Double,
    val throttlePositionPct: Double,
    val fuelLevelPct: Double,
    val batteryVoltageV: Double,
    val latitude: Double?,
    val longitude: Double?,
    val heading: Double?,
    val activeDtcCodes: String?
)
data class TelemetryAckResponse(val status: String, val timestamp: String)

// 7. Diagnostics DTOs
data class DtcResponse(val code: String, val description: String, val severity: String, val system: String, val symptoms: String?, val potentialCauses: String?, val recommendedAction: String?)

// 8. AI Orchestrator DTOs
data class DriverContext(val name: String, val experienceLevel: String, val tonePreference: String)
data class VehicleContext(val make: String, val model: String, val year: Int?, val fuelType: String?)
data class TelemetryContext(
    val speedKmh: Double,
    val rpm: Int,
    val coolantTempC: Double,
    val batteryVoltage: Double,
    val fuelLevelPct: Double,
    val throttlePositionPct: Double,
    val dtcActive: List<String>
)
data class TripContext(val durationMinutes: Int, val distanceKm: Double, val startLocation: String?, val destination: String?)

data class AiConversationRequest(
    val systemInstruction: String?,
    val driver: DriverContext,
    val vehicle: VehicleContext,
    val telemetry: TelemetryContext,
    val trip: TripContext?,
    val ephemeralMemory: List<String>,
    val userQuery: String
)

data class AiConversationResponse(
    val spokenResponse: String,
    val displaySummary: String,
    val category: String,
    val criticalAlert: Boolean,
    val suggestedFollowUps: List<String>,
    val timestamp: String
)

// 9. Reminders DTOs
data class CreateReminderRequest(val vehicleId: UUID?, val title: String, val reminderType: String, val dueOdometerKm: Double?, val dueDate: String?, val notes: String?)
data class ReminderResponse(val id: UUID, val vehicleId: UUID?, val title: String, val reminderType: String, val dueOdometerKm: Double?, val dueDate: String?, val completed: Boolean, val notes: String?)

// 10. Privacy & DPDPA DTOs
data class PrivacySummaryResponse(val registeredVehicles: Int, val recordedTrips: Int, val storedReminders: Int, val dataRetentionPolicy: String)
data class PrivacyPurgeResponse(val status: String, val message: String, val deletedSnapshots: Long, val deletedTrips: Long, val deletedAlerts: Long, val deletedReminders: Long)
