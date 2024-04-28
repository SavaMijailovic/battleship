package app.battleship

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
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

        setPlayers()
        setPlayersNames()
        setButtonListeners()

        game.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        game.interrupt()
        GameManager.reset()
    }

    override fun finish() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        super.finish()
    }

    private val player1: Player = GameManager.player1 as Player
    private val player2: Player = GameManager.player2 as Player

    private val tvActivePlayer: TextView by lazy { findViewById(R.id.tvActivePLayer) }
    private val tvPlayer1: TextView by lazy { findViewById(R.id.tvPlayer1) }
    private val tvPlayer2: TextView by lazy { findViewById(R.id.tvPlayer2) }
    private val tvScore: TextView by lazy { findViewById(R.id.tvScore) }

    private val game = Thread {
        var activePlayer = player1
        var otherPlayer = player2

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
        }
        catch (_: InterruptedException) {}
    }

    private fun processWinner(player: Player) {
        runOnUiThread {
            if (player == player1) {
                tvPlayer1.setTextColor(getColor(R.color.win))
                tvPlayer2.setTextColor(getColor(R.color.lose))
                tvPlayer1.append("\uD83D\uDC51")
            }
            else {
                tvPlayer1.setTextColor(getColor(R.color.lose))
                tvPlayer2.setTextColor(getColor(R.color.win))
                tvPlayer2.append("\uD83D\uDC51")
            }

            Toast.makeText(this, "Winner is $player", Toast.LENGTH_LONG).show()
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