package app.battleship

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        hideSystemUI(window)

        resize()

        setPlayersNames()
        setPlayers()
        setButtonListeners()

        setBoardListeners(player1 as DevicePlayer)
        
        if (gamemode == Gamemode.MULTIPLAYER_DEVICE) {
            setBoardListeners(player2 as DevicePlayer)
        }

        // Thread(::game).start()
    }

    private val gamemode: Gamemode = GameManager.gamemode
    private val player1: Player = GameManager.player1 as Player
    private val player2: Player = GameManager.player2 as Player

    private fun game() {
        var activePlayer = player1
        var otherPlayer = player2

        while (true) {
            if (activePlayer.play()) {
                if (otherPlayer.health == 0) break
            }
            else {
                activePlayer = otherPlayer.also { otherPlayer = activePlayer }
            }
        }

        processWinner(activePlayer)
    }

    private fun processWinner(player: Player) {
        Looper.prepare()
        Toast.makeText(this, "Winner is $player", Toast.LENGTH_LONG).show()
        // TODO
    }

    private fun setPlayersNames() {
        findViewById<TextView>(R.id.tvPlayer1).apply {
            text = player1.name
        }
        
        findViewById<TextView>(R.id.tvPlayer2).apply {
            text = player2.name
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

    override fun finish() {
        GameManager.reset()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun setButtonListeners() {
        findViewById<ImageButton>(R.id.btBack).setOnClickListener {
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBoardListeners(player: DevicePlayer) {
        val board = player.opponentBoard

        board.layout?.setOnTouchListener { _, event ->
            if (!board.active) {
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    board.forEach { field, tv ->

                        val location = IntArray(2)
                        tv.getLocationOnScreen(location)
                        val (x, y) = location

                        if (!board.isInside(event.rawX, event.rawY)) {
                            field.background?.color = Color.TRANSPARENT
                        }
                        else if (event.rawX >= x && event.rawX <= x + tv.width &&
                            event.rawY >= y && event.rawY <= y + tv.height) {

                            field.background?.color = Color.DKGRAY
                        }
                        else if ((event.rawX >= x && event.rawX <= x + tv.width) ||
                            (event.rawY >= y && event.rawY <= y + tv.height)) {

                            field.background?.color = Color.LTGRAY
                        }
                        else {
                            field.background?.color = Color.TRANSPARENT
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    board.forEach { field, tv ->
                        field.background?.color = Color.TRANSPARENT

                        val location = IntArray(2)
                        tv.getLocationOnScreen(location)
                        val (x, y) = location

                        if (event.rawX >= x && event.rawX <= x + tv.width &&
                            event.rawY >= y && event.rawY <= y + tv.height) {

                            field.state = when (field.state) {
                                Field.State.UNKNOWN -> Field.State.EMPTY
                                Field.State.EMPTY -> Field.State.SHIP
                                Field.State.SHIP -> Field.State.DESTROYED_SHIP
                                Field.State.DESTROYED_SHIP -> Field.State.UNKNOWN
                            }
                        }
                    }
                    true
                }

                else -> false
            }
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
    }
}