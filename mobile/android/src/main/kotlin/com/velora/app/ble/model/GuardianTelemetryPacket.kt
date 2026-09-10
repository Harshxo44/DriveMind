package com.velora.app.ble.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Kotlin representation of the 16-byte packed binary BLE packet received from Guardian.
 *
 * (RPM, Speed, Coolant/Odometer, Fuel, DTCs, Battery, Engine State Machine Flags)
 */
data class GuardianTelemetryPacket(
    val rpm: Int,                 // Bytes 1-2: Short (unsigned)
    val speedKmh: Int,           // Byte  3: uint8
    val coolantTempC: Int,        // Bytes 4-5: Short (2 bytes signed)
    val throttlePct: Int,         // Byte  6: uint8 (0-100%)
    val fuelLevelPct: Int,        // Byte  7: uint8 (0-100%)
    val batteryMv: Int,           // Bytes 8-9: Short (unsigned e.g. 14100 = 14.10V)
    val engineState: Int,         // Byte 10: uint8 State machine flags
    val dtcCount: Int,            // Byte 11: uint8 active codes count
    val sequenceId: Int           // Bytes 12-13: uint16
) {
    companion object {
        const val PACKET_SIZE = 16 // Bytes
        const val MAGIC_BYTE = 0x47.toByte() // 'G'

        /**
         * Efficiently parses a 16-byte buffer into a data object.
         */
        fun parse(data: ByteArray): GuardianTelemetryPacket? {
            if (data.size != PACKET_SIZE) return null

            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            // Validate Frame Sync Magic Byte
            if (buffer.get() != MAGIC_BYTE) return null

            // Read primitive fields
            val rpm = buffer.short.toInt() and 0xFFFF // Short to Int (unsigned shift)
            val speed = buffer.get().toInt() and 0xFF // Byte to Int (unsigned shift)
            val coolant = buffer.short.toInt() // Signed temperature
            val throttle = buffer.get().toInt() and 0xFF
            val fuel = buffer.get().toInt() and 0xFF
            val battery = buffer.short.toInt() and 0xFFFF
            val engineState = buffer.get().toInt() and 0xFF
            val dtcCount = buffer.get().toInt() and 0xFF
            val sequenceId = buffer.short.toInt() and 0xFFFF

            // We do not validate the Fletcher-16 checksum here (handled by native BLE buffer validations if enabled).

            return GuardianTelemetryPacket(
                rpm = rpm,
                speedKmh = speed,
                coolantTempC = coolant,
                throttlePct = throttle,
                fuelLevelPct = fuel,
                batteryMv = battery,
                engineState = engineState,
                dtcCount = dtcCount,
                sequenceId = sequenceId
            )
        }
    }
}
