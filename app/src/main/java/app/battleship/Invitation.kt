package app.battleship

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import app.battleship.Invitation.Data.*
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("MissingPermission")
class Invitation(
    private val context: Activity,
    private val connection: BluetoothConnection,
    private val action: (data: Data?) -> Unit
) : Thread() {

    companion object {
        private val isInvited = AtomicBoolean(false)

        fun reset() {
            isInvited.set(false)
        }
    }

    private var dialog: Dialog? = null

    private val deviceName = connection.socket.remoteDevice.name ?: "other player"

    private fun readData() : Data? {
        return try {
            Data.valueOf(connection.read())
        }
        catch (_: IllegalArgumentException) {
            null
        }
    }

    fun invite() {
        try {
            if (!connection.isConnected()) {
                throw Exception()
            }

            if (isInvited.getAndSet(true)) {
                return
            }
            connection.write(INVITE)

            showDialog {
                AlertDialog.Builder(context)
                    .setTitle("Waiting for $deviceName...")
                    .setNegativeButton("Cancel") { _, _ ->
                        isInvited.set(false)
                        connection.write(CANCEL)
                    }
                    .create()
                    .apply {
                        setCanceledOnTouchOutside(false)
                    }
            }
            return
        }
        catch (_: Exception) {}

        showDialog {
            AlertDialog.Builder(context)
                .setTitle("Connection lost")
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
        }
    }

    override fun run() {
        if (!connection.isConnected()) return

        var data: Data?

        try {
            while (true) {
                data = readData()

                when (data) {
                    START, ERROR, null -> break

                    INVITE -> {
                        if (isInvited.getAndSet(true)) {
                            connection.write(DECLINED)
                            continue
                        }

                        var response = DECLINED
                        showDialog {
                            AlertDialog.Builder(context)
                                .setTitle("Invite from $deviceName")
                                .setPositiveButton("Accept") { _, _ ->
                                    response = ACCEPTED
                                }
                                .setNegativeButton("Decline") { _, _ ->
                                    response = DECLINED
                                }
                                .setOnDismissListener { _ ->
                                    isInvited.set(false)
                                    connection.write(response)
                                }
                                .create()
                        }
                    }

                    ACCEPTED -> {
                        if (isInvited.get()) {
                            connection.write(START)
                            break
                        }
                        else {
                            connection.write(CANCEL)
                        }
                    }

                    DECLINED -> {
                        if (isInvited.getAndSet(false)) {
                            showDialog {
                                AlertDialog.Builder(context)
                                    .setTitle("$deviceName declined invite")
                                    .setNegativeButton("Cancel") { _, _ -> }
                                    .create()
                            }
                        }
                    }

                    CANCEL -> {
                        if (isInvited.getAndSet(false)) {
                            context.runOnUiThread { dialog?.dismiss() }
                        }
                    }
                }
            }
        }
        catch (_: Exception) {
            data = ERROR
        }

        action(data)
    }

    private fun showDialog(makeDialog: () -> Dialog?) {
        context.runOnUiThread {
            dialog?.dismiss()
            dialog = makeDialog()
            dialog?.show()
        }
    }

    enum class Data { INVITE, ACCEPTED, DECLINED, START, CANCEL, ERROR }
}