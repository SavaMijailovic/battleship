package app.battleship

abstract class Player(var name: String) {

    companion object {
        const val START_HEALTH = 20
    }

    lateinit var opponent: Player
    open lateinit var opponentBoard: Board

    var health: Int = START_HEALTH
        set(value) { field = value.coerceIn(0, START_HEALTH) }

    abstract fun nextTarget() : Field
    abstract fun fire(target: Field) : Pair<Boolean, Boolean>
    abstract fun isReady() : Boolean

    fun play() : Boolean? {
        val target = nextTarget()
        if (!isValid(target)) return null
        val (hit, destroyed) = opponent.fire(target)
        opponentBoard.update(target, hit, destroyed)
        return hit
    }

    protected fun isValid(target: Field) : Boolean {
        return opponentBoard.isInside(target) && opponentBoard[target].state == Field.State.UNKNOWN
    }

    override fun toString() : String = name
}