package app.battleship

object GameManager {
    var gamemode: Gamemode = Gamemode.SINGLEPLAYER
    var player1: Player? = null
    var player2: Player? = null

    fun setPlayers(player1: Player?, player2: Player?) {
        this.player1 = player1
        this.player2 = player2
    }

    fun isReady() : Boolean {
        return player1?.isReady() == true && player2?.isReady() == true
    }

    fun reset() {
        player1 = null
        player2 = null
    }
}