package app.battleship

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemUI(window)

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
            GameManager.player1 = BotPlayer("Bot1")
            GameManager.player2 = BotPlayer("Bot2")
            startActivity(Intent(this, GameActivity::class.java))
        }
    }
}