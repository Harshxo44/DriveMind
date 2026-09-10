package com.velora.app.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.velora.app.ble.model.GuardianTelemetryPacket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * Manages BLE connection to the Guardian ESP32 device and streams telemetry packets.
 */
@SuppressLint("MissingPermission")
class BleTelemetryManager(private val context: Context) {

    companion object {
        private const val TAG = "BleTelemetryManager"
        val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val TELEMETRY_CHAR_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private val _telemetryStream = MutableSharedFlow<GuardianTelemetryPacket>(replay = 1)
    val telemetryStream: SharedFlow<GuardianTelemetryPacket> = _telemetryStream.asSharedFlow()

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to Guardian BLE Device. Requesting MTU 64...")
                gatt?.requestMtu(64)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "Disconnected from Guardian BLE Device.")
                bluetoothGatt = null
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            Log.i(TAG, "MTU changed to $mtu. Discovering services...")
            gatt?.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(TELEMETRY_CHAR_UUID)

                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(CCCD_UUID)
                    descriptor?.let {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(it)
                        Log.i(TAG, "Subscribed to Guardian Telemetry Notifications (5 Hz)")
                    }
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { rawBytes ->
                val packet = GuardianTelemetryPacket.parse(rawBytes)
                if (packet != null) {
                    _telemetryStream.tryEmit(packet)
                } else {
                    Log.w(TAG, "Received corrupted or non-matching telemetry packet (${rawBytes.size} bytes)")
                }
            }
        }
    }

    fun connect(device: BluetoothDevice) {
        Log.i(TAG, "Initiating connection to ${device.address}...")
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
