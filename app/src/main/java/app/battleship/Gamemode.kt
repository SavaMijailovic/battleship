package app.battleship

import java.io.Serializable

enum class Gamemode : Serializable {
    SINGLEPLAYER, MULTIPLAYER_DEVICE;

    override fun toString(): String {
        return when (this) {
            SINGLEPLAYER -> "Singleplayer"
            MULTIPLAYER_DEVICE -> "Multiplayer on device"
        }
    }
}