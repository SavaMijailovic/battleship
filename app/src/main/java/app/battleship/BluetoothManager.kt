package app.battleship

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.location.LocationManager

object BluetoothManager {

    lateinit var adapter: BluetoothAdapter
        @Synchronized get

    private lateinit var locationManager: LocationManager

    var isSupported: Boolean = false

    var connection: BluetoothConnection? = null
        @Synchronized get
        @Synchronized set

    fun init(context: Context) {
        val adapter = context.getSystemService(android.bluetooth.BluetoothManager::class.java).adapter
        if (adapter != null) {
            isSupported = true
            this.adapter = adapter
        }
        locationManager = context.getSystemService(LocationManager::class.java)
    }

    fun isBluetoothEnabled() : Boolean {
        return adapter.isEnabled
    }

    fun isLocationEnabled() : Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun reset() {
        connection?.close()
        connection = null
    }
}