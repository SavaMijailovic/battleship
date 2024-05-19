package app.battleship

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemUI(window)

        GameManager.reset(true)
        BluetoothManager.reset()
        BluetoothManager.init(this)

        findViewById<Button>(R.id.btSingleplayer).setOnClickListener {
            GameManager.gamemode = Gamemode.SINGLEPLAYER
            startActivity(Intent(this, ShipsPlacementActivity::class.java))
        }

        findViewById<Button>(R.id.btMultiplayerDevice).setOnClickListener {
            GameManager.gamemode = Gamemode.MULTIPLAYER_DEVICE
            startActivity(Intent(this, ShipsPlacementActivity::class.java))
        }

        findViewById<Button>(R.id.btTwoBots).setOnClickListener {
            GameManager.gamemode = Gamemode.SINGLEPLAYER
            startActivity(Intent(this, GameActivity::class.java))
        }

        findViewById<Button>(R.id.btMultiplayerBluetooth).setOnClickListener {
            GameManager.gamemode = Gamemode.MULTIPLAYER_BLUETOOTH
            startBluetoothConnectionActivity()
        }
    }

    override fun onResume() {
        GameManager.reset(true)
        BluetoothManager.reset()
        super.onResume()
    }

    private fun startBluetoothConnectionActivity() {
        if (!BluetoothManager.isSupported) {
            AlertDialog.Builder(this)
                .setTitle("Bluetooth Not Supported")
                .setMessage("Your device does not support bluetooth")
                .create()
                .show()
            return
        }

        if (!permissionsGranted(bluetoothPermissions)) return

        if (!permissionsGranted(locationPermissions)) return

        if (!BluetoothManager.isLocationEnabled()) {
            launcherRequest.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        if (!BluetoothManager.isBluetoothEnabled()) {
            launcherRequest.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        startActivity(Intent(this, BluetoothConnectionActivity::class.java))
    }

    private val bluetoothPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
        else {
            arrayOf()
        }

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun permissionsGranted(permissions: Array<String>) : Boolean {
        if (permissions.isEmpty()) return true

        val deniedPermissions = permissions.filter { permission ->
            ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            permissionsRequest.launch(deniedPermissions.toTypedArray())
            return false
        }

        return true
    }

    private val launcherRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startBluetoothConnectionActivity()
        }
    }

    private val permissionsRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }

        if (allPermissionsGranted) {
            startBluetoothConnectionActivity()
        }
        else {
            AlertDialog.Builder(this)
                .setTitle("Permissions not granted")
                .setPositiveButton("Grant") { _, _ ->
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    })
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
                .show()
        }
    }
}