package app.battleship

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.ceil

@SuppressLint("MissingPermission")
class BluetoothConnectionActivity : AppCompatActivity() {
    private companion object {
        private val CONNECTION_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fa")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_connection)
        hideSystemUI(window)

        resize()

        setUpButtons()
        Invitation.reset()

        registerReceiver()
        makeDiscoverable()
        server.start()
        discovery.start()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        discovery.cancel()
        server.cancel()
        synchronized(sockets) {
            sockets.forEach(BluetoothSocket::close)
        }
        synchronized(connectedDevices) {
            connectedDevices.forEach { (_, socket) ->
                socket.close()
            }
        }
        super.onDestroy()
    }

    override fun finish() {
        BluetoothManager.connection?.close()
        super.finish()
    }

    private fun setUpButtons() {
        findViewById<ImageButton>(R.id.btBack).setOnClickListener {
            finish()
        }

        val btSearching = findViewById<ImageButton>(R.id.btSearching)

        btSearching.setOnClickListener {
            makeDiscoverable()
        }

        Glide.with(this).load(R.drawable.searching).centerCrop().into(btSearching)
    }

    private val server = object : Thread() {
        private lateinit var serverSocket: BluetoothServerSocket

        override fun run() {
            serverSocket = BluetoothManager.adapter.listenUsingInsecureRfcommWithServiceRecord(packageName, CONNECTION_UUID)
            try {
                while (true) {
                    watchSocket(serverSocket.accept())
                }
            }
            catch (_: Exception) {}
        }

        fun cancel() {
            if (::serverSocket.isInitialized) {
                serverSocket.close()
            }
        }
    }

    private val discovery = object : Thread() {
        private val DISCOVERY_DURATION = 5000L
        private val PAUSE_DURATION = 2000L

        override fun run() {
            try {
                while (!interrupted()) {
                    BluetoothManager.adapter.startDiscovery()
                    runBlocking { delay(DISCOVERY_DURATION) }
                    if (interrupted()) break
                    BluetoothManager.adapter.cancelDiscovery()
                    runBlocking { delay(PAUSE_DURATION) }
                }
            }
            catch (_: Exception) {}
            BluetoothManager.adapter.cancelDiscovery()
        }

        fun cancel() {
            interrupt()
        }
    }

    private val connectedDevices = TreeMap<BluetoothDevice, BluetoothSocket> { d1, d2 ->
        d1.address.compareTo(d2.address)
    }

    private val sockets = TreeSet<BluetoothSocket> { s1, s2 ->
        s1.remoteDevice.address.compareTo(s2.remoteDevice.address)
    }

    private val layoutDevices: LinearLayout by lazy { findViewById(R.id.layoutDevices) }

    private fun registerReceiver() {
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Thread {
                        try {
                            val device: BluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                            }
                            else {
                                @Suppress("DEPRECATION")
                                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            } ?: return@Thread

                            synchronized(connectedDevices) {
                                if (connectedDevices.contains(device)) {
                                    return@Thread
                                }
                            }

                            val socket: BluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(CONNECTION_UUID)

                            synchronized(sockets) {
                                if (!sockets.add(socket)) {
                                    return@Thread
                                }
                            }

                            try {
                                socket.connect()
                            }
                            catch (_: Exception) {}

                            synchronized(sockets) {
                                sockets.remove(socket)
                                if (socket.isConnected) {
                                    watchSocket(socket)
                                }
                                else {
                                    socket.close()
                                }
                            }
                        }
                        catch (_: Exception) {}
                    }.start()
                }

                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                    val scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE)
                    if (scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        makeDiscoverable()
                    }
                }
            }
        }
    }

    private fun makeDiscoverable(duration: Int = 300) {
        if (BluetoothManager.adapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
            })
        }
    }

    private fun watchSocket(socket: BluetoothSocket) {
        synchronized(connectedDevices) {
            if (connectedDevices.contains(socket.remoteDevice)) {
                socket.close()
                return
            }
            connectedDevices[socket.remoteDevice] = socket
        }

        val connection = BluetoothConnection(socket)

        val gameInvitation = Invitation(this, connection) { data ->
            if (data == Invitation.Data.ERROR) {
                unwatchSocket(socket)
            }
            else {
                if (data == Invitation.Data.START) {
                    GameManager.changeFirstToPlay()
                }
                startShipsPlacementActivity(connection)
            }
        }

        runOnUiThread {
            val view = addDeviceView(socket.remoteDevice)

            view.tag = socket.remoteDevice

            view.setOnClickListener {
                gameInvitation.invite()
            }
        }

        gameInvitation.start()
    }

    private fun unwatchSocket(socket: BluetoothSocket) {
        synchronized(connectedDevices) {
            removeDeviceView(socket.remoteDevice)
            connectedDevices[socket.remoteDevice]?.close()
            connectedDevices.remove(socket.remoteDevice)
        }
    }

    private fun startShipsPlacementActivity(connection: BluetoothConnection) {
        BluetoothManager.connection = connection
        synchronized(connectedDevices) {
            connectedDevices.remove(connection.socket.remoteDevice)
        }
        runOnUiThread {
            startActivity(Intent(this, ShipsPlacementActivity::class.java))
            super.finish()
        }
    }

    private fun removeDeviceView(device: BluetoothDevice) {
        runOnUiThread {
            for (i in layoutDevices.childCount - 1 downTo 0) {
                val view = layoutDevices.getChildAt(i) ?: continue
                if (view.tag === device) {
                    layoutDevices.removeViewAt(i)
                    if (i > 0) {
                        layoutDevices.removeViewAt(i - 1)
                    }
                    else if (layoutDevices.childCount > 0) {
                        layoutDevices.removeViewAt(i)
                    }
                }
            }
        }
    }

    private val size by lazy { findViewById<ScrollView>(R.id.svDevices).layoutParams.height / 10 }

    private fun addDeviceView(device: BluetoothDevice) : TextView {
        val view = TextView(this)
        runOnUiThread {
            if (layoutDevices.children.count() > 0) {
                layoutDevices.addView(TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ceil(0.7 * size).toInt()
                    )
                })
            }

            view.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ceil(1.3 * size).toInt()
                )
                setTextColor(getColor(R.color.text))
                setTextSize(TypedValue.COMPLEX_UNIT_PX, layoutParams.height * 0.5f)
                text = device.name ?: "Unknown"
                gravity = Gravity.CENTER
                background = BorderDrawable(color = getColor(R.color.button))
            }

            layoutDevices.addView(view)
        }
        return view
    }

    private fun resize() {
        val size = getUnitSize(resources)

        findViewById<TextView>(R.id.tvSearching).apply {
            layoutParams.apply {
                width = 12 * size
                height = 2 * size
                setTextSize(TypedValue.COMPLEX_UNIT_PX, height * 0.5f)
            }
        }

        findViewById<ScrollView>(R.id.svDevices).apply {
            layoutParams.apply {
                width = 12 * size
                height = 10 * size
            }
        }

        findViewById<ImageButton>(R.id.btBack).apply {
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                marginEnd = size
                width = 2 * size
                height = 2 * size
            }
        }

        findViewById<ImageButton>(R.id.btSearching).apply {
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                marginStart = size
                width = 2 * size
                height = 2 * size
            }
        }
    }
}