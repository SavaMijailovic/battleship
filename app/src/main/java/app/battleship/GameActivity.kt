package app.battleship

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.concurrent.atomic.AtomicBoolean

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        hideSystemUI(window)

        resize()

        setPlayers()
        setPlayersNames()
        setButtonListeners()
        Invitation.reset()

        game.start()
    }

    override fun onDestroy() {
        game.interrupt()
        super.onDestroy()
    }

    override fun finish() {
        if (game.isAlive) {
            AlertDialog.Builder(this)
                .setTitle("Do you want to leave the game?")
                .setPositiveButton("Stay") { _, _ -> }
                .setNegativeButton("Leave") { _, _ ->
                    leave()
                }
                .create()
                .show()
        }
        else {
            AlertDialog.Builder(this)
                .setTitle("Do you want to play again?")
                .setNeutralButton("Cancel") { _, _ -> }
                .setPositiveButton("Rematch") { _, _ ->
                    playAgain()
                }
                .setNegativeButton("Leave") { _, _ ->
                    leave()
                }
                .create()
                .show()
        }
    }

    private var leaving = false
    private val otherPlayerLeft = AtomicBoolean(false)

    private fun leave() {
        leaving = true
        if (GameManager.gamemode == Gamemode.MULTIPLAYER_BLUETOOTH) {
            BluetoothManager.connection?.write(BluetoothPlayer.LEAVE)
            BluetoothManager.connection?.close()
        }
        super.finish()
    }

    private fun playAgain() {
        if (player1::class == BotPlayer::class && player2::class == BotPlayer::class) {
            playAgain(GameActivity::class.java)
        }
        else if (GameManager.gamemode == Gamemode.MULTIPLAYER_BLUETOOTH) {
            if (otherPlayerLeft.get()) {
                showDialog("Other player left")
            }
            else {
                bluetoothInvitation?.invite()
            }
        }
        else {
            playAgain(ShipsPlacementActivity::class.java)
        }
    }

    private fun <T> playAgain(activity: Class<T>) {
        GameManager.reset()
        GameManager.changeFirstToPlay()
        startActivity(Intent(this, activity))
        super.finish()
    }

    private val bluetoothInvitation by lazy {
        try {
            Invitation(this, BluetoothManager.connection!!) { data ->
                if (data == null || data == Invitation.Data.ERROR) {
                    if (data == null) {
                        otherPlayerLeft.set(true)
                    }
                    BluetoothManager.connection?.close()
                }
                else {
                    playAgain(ShipsPlacementActivity::class.java)
                }
            }
        }
        catch (_: Exception) {
            null
        }
    }

    private val player1: Player = GameManager.player1 ?: BotPlayer("Bot1")
    private val player2: Player = GameManager.player2 ?: BotPlayer("Bot2")

    private val tvActivePlayer: TextView by lazy { findViewById(R.id.tvActivePLayer) }
    private val tvPlayer1: TextView by lazy { findViewById(R.id.tvPlayer1) }
    private val tvPlayer2: TextView by lazy { findViewById(R.id.tvPlayer2) }
    private val tvScore: TextView by lazy { findViewById(R.id.tvScore) }

    private val game = Thread {
        var activePlayer = player1
        var otherPlayer = player2

        if (!GameManager.firstToPlay) {
            activePlayer = otherPlayer.also { otherPlayer = activePlayer }
        }

        updateActivePlayer(activePlayer)
        updateScore()

        try {
            while (true) {
                val hit = activePlayer.play() ?: continue
                if (hit) {
                    updateScore()
                    if (otherPlayer.health == 0) break
                }
                else {
                    activePlayer = otherPlayer.also { otherPlayer = activePlayer }
                    updateActivePlayer(activePlayer)
                }
            }

            processWinner(activePlayer)
            if (GameManager.gamemode == Gamemode.MULTIPLAYER_BLUETOOTH) {
                bluetoothInvitation?.start()
            }
        }
        catch (_: InterruptedException) {
            BluetoothManager.connection?.close()
            return@Thread
        }
        catch (e: Exception) {
            if (e.message == BluetoothPlayer.LEAVE) {
                otherPlayerLeft.set(true)
                processWinner(player1)
            }
            else if (!leaving) {
                showDialog("Connection lost")
                runOnUiThread {
                    @SuppressLint("SetTextI18n")
                    tvPlayer2.text = "Connection lost"
                }
            }
            BluetoothManager.connection?.close()
        }
    }

    private fun processWinner(winner: Player) {
        runOnUiThread {
            val tvWinner = if (winner == player1) tvPlayer1 else tvPlayer2
            val tvLooser = if (winner == player2) tvPlayer1 else tvPlayer2

            tvWinner.apply {
                setTextColor(getColor(R.color.green))
                append("\uD83D\uDC51")
            }
            tvLooser.setTextColor(getColor(R.color.red))

            if (otherPlayerLeft.get()) {
                showDialog("Other player left. You Win")
            }
            else {
                AlertDialog.Builder(this)
                    .setTitle("Winner is $winner")
                    .setNegativeButton("Cancel") { _, _ -> }
                    .setPositiveButton("Rematch") { _, _ ->
                        playAgain()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun showDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle(message)
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateActivePlayer(player: Player) {
        runOnUiThread {
            if (player == player1) {
                tvActivePlayer.text = getString(R.string.right_arrow)
            }
            else {
                tvActivePlayer.text = getString(R.string.left_arrow)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateScore() {
        runOnUiThread {
            tvScore.text = "${Player.START_HEALTH - player2.health} : ${Player.START_HEALTH - player1.health}"
        }
    }

    private fun setPlayers() {
        val layoutBoard2 = findViewById<LinearLayout>(R.id.layoutBoard2)
        player1.opponentBoard = Board(context = this, layout = layoutBoard2, rightSide = true)
        player1.opponent = player2

        val layoutBoard1 = findViewById<LinearLayout>(R.id.layoutBoard1)
        player2.opponentBoard = Board(context = this, layout = layoutBoard1)
        player2.opponent = player1
    }

    @SuppressLint("SetTextI18n")
    private fun setPlayersNames() {
        tvPlayer1.text = "$player1"
        tvPlayer2.text = "$player2"
    }

    private fun setButtonListeners() {
        findViewById<ImageButton>(R.id.btBack).setOnClickListener {
            finish()
        }
    }

    private fun resize() {
        val size = getUnitSize(resources)

        arrayOf(R.id.layoutBoard1, R.id.layoutBoard2).forEach { id ->
            findViewById<LinearLayout>(id).apply {
                layoutParams.apply {
                    width = 11 * size
                    height = 11 * size
                }
            }
        }

        arrayOf(R.id.tvPlayer1, R.id.tvPlayer2).forEach { id ->
            findViewById<TextView>(id).apply {
                layoutParams.apply {
                    width = 11 * size
                    height = size
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, height * 0.6f)
                }
            }
        }

        findViewById<LinearLayout>(R.id.layoutMiddle).apply {
            layoutParams.apply {
                width = 2 * size
                height = 12 * size
            }
        }

        findViewById<ImageButton>(R.id.btBack).apply {
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                width = 2 * size
                height = 2 * size
                topMargin = size
            }
        }

        findViewById<TextView>(R.id.tvScore).apply {
            layoutParams.apply {
                width = 2 * size
                height = size
                setTextSize(TypedValue.COMPLEX_UNIT_PX, height * 0.5f)
            }
        }

        findViewById<TextView>(R.id.tvActivePLayer).apply {
            layoutParams.apply {
                width = 2 * size
                height = 2 * size
            }
        }
    }
}