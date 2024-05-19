package app.battleship

import java.util.Scanner

class BluetoothPlayer(name: String, private val connection: BluetoothConnection) : Player(name) {

    companion object {
        const val LEAVE = "leave"
    }

    var ready = false
        @Synchronized get
        @Synchronized set

    private fun read(action: (Scanner) -> Unit) {
        while (true) {
            val input = connection.read()
            if (input == LEAVE) {
                throw RuntimeException(LEAVE)
            }
            try {
                Scanner(input).use { sc ->
                    action(sc)
                    return
                }
            }
            catch (_: Exception) {}
        }
    }

    override fun nextTarget() : Field {
        var target = Field()
        read { sc ->
            target = Field(sc.nextInt(), sc.nextInt())
        }
        return target
    }

    override fun fire(target: Field) : Pair<Boolean, Boolean> {
        connection.write(target)

        var (hit, destroyed) = Pair(false, false)
        read { sc ->
            hit = sc.nextBoolean()
            destroyed = sc.nextBoolean()
        }

        if (hit) {
            this.health--
        }
        return Pair(hit, destroyed)
    }

    override fun processResult(target: Field, hit: Boolean, destroyed: Boolean) {
        connection.write("$hit $destroyed")
        super.processResult(target, hit, destroyed)
    }

    override fun isReady() : Boolean = ready
}