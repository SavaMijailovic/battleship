package app.battleship

abstract class Player(var name: String) {

    companion object {
        const val START_HEALTH = 20
    }

    lateinit var opponent: Player
    lateinit var opponentBoard: Board

    var health: Int = START_HEALTH
        set(value) { field = value.coerceIn(0, START_HEALTH) }

    abstract fun nextTarget() : Field
    abstract fun fire(target: Field) : Field
    abstract fun isReady() : Boolean

    fun play() : Boolean {
        val result = opponent.fire(nextTarget())
        opponentBoard.update(result)
        return result.state == Field.State.SHIP
    }

    override fun toString() : String = name
}