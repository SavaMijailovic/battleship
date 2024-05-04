package app.battleship

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class BotPlayer(
    name: String,
    board: Board = Board(random = true),
    private val delayTime: Long = 500)
    : DevicePlayer(name, board)
{

    private lateinit var fields: MutableList<Field>
    private val neighbors = mutableListOf<Pair<Field, Field>>()
    private val directions = arrayOf(Field(-1, 0), Field(1, 0), Field(0, -1), Field(0, 1))

    override var opponentBoard: Board
        get() = super.opponentBoard
        set(value) {
            super.opponentBoard = value
            fields = ArrayList()
            opponentBoard.forEach(fields::add)
            fields.shuffle()
        }

    private fun addNeighbor(field: Field, direction: Field) {
        val neighbor = Field(field.row + direction.row, field.col + direction.col)
        if (opponentBoard.isInside(neighbor) && opponentBoard[neighbor].state == Field.State.UNKNOWN) {
            neighbors.add(Pair(opponentBoard[neighbor], direction))
        }
    }

    override fun nextTarget() : Field {
        runBlocking { delay(delayTime) }

        if (neighbors.isNotEmpty() || fields.last().state == Field.State.DESTROYED_SHIP) {
            if (neighbors.isEmpty()) {
                directions.forEach { direction ->
                    addNeighbor(fields.last(), direction)
                }
                neighbors.shuffle()
            }

            while (neighbors.isNotEmpty()) {
                val neighbor = neighbors.last()

                when (neighbor.first.state) {
                    Field.State.UNKNOWN -> {
                        return neighbor.first
                    }
                    Field.State.EMPTY, Field.State.SHIP -> {
                        neighbors.removeLast()
                    }
                    Field.State.DESTROYED_SHIP -> {
                        neighbors.removeLast()
                        if (neighbors.size > 1) {
                            neighbors.removeAll {
                                (neighbor.second.row == 0 && it.second.row != 0) ||
                                (neighbor.second.col == 0 && it.second.col != 0)
                            }
                        }
                        addNeighbor(neighbor.first, neighbor.second)
                    }
                }
            }
        }

        while (fields.last().state != Field.State.UNKNOWN) {
            fields.removeLast()
        }
        return fields.last()
    }
}