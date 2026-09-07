package com.guardian.app.repository

import com.guardian.app.ble.BleTelemetryManager
import com.guardian.app.ble.model.GuardianTelemetryPacket
import com.guardian.app.network.GuardianApiService
import com.guardian.app.network.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Single source of truth repository for Guardian Android App.
 * Bridges BLE edge telemetry with Cloud REST API and in-memory ephemeral conversation storage.
 */
class GuardianRepository(
    private val apiService: GuardianApiService,
    private val bleManager: BleTelemetryManager,
    private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val telemetryStream: SharedFlow<GuardianTelemetryPacket> = bleManager.telemetryStream

    var activeVehicleId: UUID? = null
    var activeTripId: UUID? = null

    // Ephemeral RAM-only conversation memory buffer (Purged on Engine Off)
    private val ephemeralMemory = mutableListOf<String>()

    init {
        // Collect telemetry and forward periodic snapshots to cloud (1 Hz rate)
        appScope.launch {
            var sampleCount = 0
            telemetryStream.collect { pkt ->
                sampleCount++

                // Handle Engine State Transitions
                if (pkt.engineState == 3) { // ENGINE_STATE_ENGINE_OFF
                    purgeEphemeralMemory()
                }

                // Ingest every 5th packet (1 Hz cloud rate)
                if (sampleCount % 5 == 0 && activeVehicleId != null) {
                    try {
                        apiService.ingestTelemetry(
                            IngestTelemetryRequest(
                                vehicleId = activeVehicleId!!,
                                tripId = activeTripId,
                                speedKmh = pkt.speedKmh.toDouble(),
                                rpm = pkt.rpm,
                                engineCoolantTempC = pkt.coolantTempC.toDouble(),
                                throttlePositionPct = pkt.throttlePct.toDouble(),
                                fuelLevelPct = pkt.fuelLevelPct.toDouble(),
                                batteryVoltageV = pkt.batteryMv / 1000.0,
                                latitude = null,
                                longitude = null,
                                heading = null,
                                activeDtcCodes = if (pkt.dtcCount > 0) "DTC_ACTIVE" else ""
                            )
                        )
                    } catch (e: Exception) {
                        // Suppress background sync errors gracefully
                    }
                }
            }
        }
    }

    suspend fun queryAiCoDriver(query: String, latestPkt: GuardianTelemetryPacket?): Result<AiConversationResponse> {
        return try {
            val req = AiConversationRequest(
                systemInstruction = "You are Guardian, an in-car AI co-driver. Keep answers under 2 sentences. Prioritize driver safety.",
                driver = DriverContext(name = "Driver", experienceLevel = "intermediate", tonePreference = "concise"),
                vehicle = VehicleContext(make = "Vehicle", model = "Model", year = 2024, fuelType = "Petrol"),
                telemetry = TelemetryContext(
                    speedKmh = latestPkt?.speedKmh?.toDouble() ?: 0.0,
                    rpm = latestPkt?.rpm ?: 0,
                    coolantTempC = latestPkt?.coolantTempC?.toDouble() ?: 90.0,
                    batteryVoltage = (latestPkt?.batteryMv ?: 14000) / 1000.0,
                    fuelLevelPct = latestPkt?.fuelLevelPct?.toDouble() ?: 50.0,
                    throttlePositionPct = latestPkt?.throttlePct?.toDouble() ?: 0.0,
                    dtcActive = if (latestPkt != null && latestPkt.dtcCount > 0) listOf("DTC_ACTIVE") else emptyList()
                ),
                trip = null,
                ephemeralMemory = ephemeralMemory.toList(),
                userQuery = query
            )
            val response = apiService.queryAiConversation(req)
            if (response.isSuccessful && response.body() != null) {
                // Record to ephemeral RAM memory
                ephemeralMemory.add("Q: $query | A: ${response.body()!!.spokenResponse}")
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("AI Service Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun purgeEphemeralMemory() {
        ephemeralMemory.clear()
    }
}
