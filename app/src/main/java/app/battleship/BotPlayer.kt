package app.battleship

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class BotPlayer(
    name: String,
    board: Board = Board(random = true),
    private val delayTime: Long = 500)
    : DevicePlayer(name, board)
{

    private val fields = mutableListOf<Field>()

    init {
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                fields.add(Field(row, col))
            }
        }
        fields.shuffle()
    }

    override fun nextTarget() : Field {
        runBlocking { delay(delayTime) }
        return fields.last().also { fields.removeLast() }
    }
}