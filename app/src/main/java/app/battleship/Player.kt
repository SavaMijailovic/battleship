package app.battleship

abstract class Player(var name: String) {

    companion object {
        const val START_HEALTH = 20
    }

    lateinit var opponent: Player
    lateinit var opponentBoard: Board
    var health: Int = START_HEALTH

    abstract fun nextTarget() : Field
    abstract fun fire(target: Field) : Pair<Field.State, Ship?>
    abstract fun isReady() : Boolean

    fun play() : Boolean {
        // TODO
        return false
    }

    override fun toString() : String = name
}