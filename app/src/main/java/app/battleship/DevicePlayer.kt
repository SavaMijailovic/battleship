package app.battleship

open class DevicePlayer(name: String, private val board: Board) : Player(name) {
    override fun nextTarget(): Field {
        // TODO
        return Field(0, 0)
    }

    override fun fire(target: Field): Pair<Field.State, Ship?> {
        // TODO
        return Pair(Field.State.UNKNOWN, null)
    }

    override fun isReady(): Boolean {
        // TODO
        return true
    }
}