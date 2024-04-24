package app.battleship

open class DevicePlayer(name: String, private val board: Board) : Player(name) {

    private val lock = Object()

    var target: Field? = null
        set(value) {
            synchronized(lock) {
                field = value
                if (value != null && opponentBoard[value].state == Field.State.UNKNOWN) {
                    lock.notify()
                }
            }
        }

    override fun nextTarget() : Field {
        opponentBoard.active = true
        synchronized(lock) {
            while (target == null) {
                lock.wait()
            }
        }
        opponentBoard.active = false
        return target.also { target = null } as Field
    }

    override fun fire(target: Field) : Field {
        val field = board[target]
        if (field.isShip()) {
            field.ship!!.health--
            this.health--
        }
        return field
    }

    override fun isReady() : Boolean {
        var count = 0
        board.forEach { field -> if (field.isShip()) count++ }
        return count == health
    }
}