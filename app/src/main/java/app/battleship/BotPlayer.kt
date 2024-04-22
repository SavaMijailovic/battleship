package app.battleship

class BotPlayer(name: String, board: Board) : DevicePlayer(name, board) {
    override fun nextTarget(): Field {
        // TODO
        return Field(0, 0)
    }
}