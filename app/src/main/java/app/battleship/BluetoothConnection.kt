package app.battleship

import android.bluetooth.BluetoothSocket
import java.io.Closeable
import java.io.PrintStream
import java.util.Scanner

open class BluetoothConnection(
    val socket: BluetoothSocket,
    input: Scanner? = null,
    output: PrintStream? = null
) : Closeable {

    private val input = input ?: Scanner(socket.inputStream)
    private val output = output ?: PrintStream(socket.outputStream, true)

    fun write(value: Any?) {
        output.println(value)
    }

    fun read() : String {
        return input.nextLine()
    }

    @Synchronized
    override fun close() {
        socket.close()
    }

    @Synchronized
    fun isConnected() : Boolean = socket.isConnected
}