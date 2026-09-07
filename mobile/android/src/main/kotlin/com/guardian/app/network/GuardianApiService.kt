package com.guardian.app.network

import com.guardian.app.network.dto.*
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

/**
 * Guardian V1 Retrofit API Interface matching GUARDIAN_API_CONTRACT.md
 */
interface GuardianApiService {

    // 1. Auth Endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // 2. Driver Profile
    @GET("drivers/profile")
    suspend fun getDriverProfile(): Response<DriverProfileResponse>

    @PUT("drivers/profile")
    suspend fun updateDriverProfile(@Body request: UpdateDriverProfileRequest): Response<DriverProfileResponse>

    // 3. Vehicles
    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleResponse>>

    @POST("vehicles")
    suspend fun registerVehicle(@Body request: CreateVehicleRequest): Response<VehicleResponse>

    @GET("vehicles/{id}")
    suspend fun getVehicleDetails(@Path("id") id: UUID): Response<VehicleResponse>

    // 4. Devices
    @POST("devices")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<DeviceResponse>

    @POST("devices/{id}/pair")
    suspend fun pairDevice(@Path("id") id: UUID, @Body request: PairDeviceRequest): Response<DeviceResponse>

    // 5. Trips
    @POST("trips/start")
    suspend fun startTrip(@Body request: StartTripRequest): Response<TripResponse>

    @POST("trips/{id}/end")
    suspend fun endTrip(@Path("id") id: UUID, @Body request: EndTripRequest): Response<TripResponse>

    @GET("trips/my")
    suspend fun getMyTrips(): Response<List<TripResponse>>

    // 6. Telemetry Ingest
    @POST("telemetry/ingest")
    suspend fun ingestTelemetry(@Body request: IngestTelemetryRequest): Response<TelemetryAckResponse>

    // 7. Diagnostics
    @GET("diagnostics/dtc/{code}")
    suspend fun lookupDtc(@Path("code") code: String): Response<DtcResponse>

    // 8. AI Conversation Orchestrator
    @POST("ai/conversation")
    suspend fun queryAiConversation(@Body request: AiConversationRequest): Response<AiConversationResponse>

    // 9. Reminders
    @GET("reminders")
    suspend fun getReminders(): Response<List<ReminderResponse>>

    @POST("reminders")
    suspend fun createReminder(@Body request: CreateReminderRequest): Response<ReminderResponse>

    @PUT("reminders/{id}/complete")
    suspend fun completeReminder(@Path("id") id: UUID): Response<ReminderResponse>

    // 10. Privacy & DPDPA
    @GET("privacy/summary")
    suspend fun getPrivacySummary(): Response<PrivacySummaryResponse>

    @DELETE("privacy/purge-my-data")
    suspend fun purgeMyData(): Response<PrivacyPurgeResponse>
}
