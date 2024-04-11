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

        val btSingleplayer = findViewById<Button>(R.id.btSingleplayer)
        btSingleplayer.setOnClickListener {
            val intent = Intent(this, ShipsPlacementActivity::class.java)
            startActivity(intent)
        }
    }
}