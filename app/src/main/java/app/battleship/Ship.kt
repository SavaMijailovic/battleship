package app.battleship

import android.view.View
import android.widget.LinearLayout
import java.util.TreeSet

class Ship(val size: Int, var view: LinearLayout? = null) {

    var health: Int = 0
        set(value) { field = value.coerceAtLeast(0) }

    private var direction: Direction = Direction.RIGHT

    var fields: TreeSet<Field> = TreeSet()

    var horizontal: Boolean = true

    init {
        health = size
    }

    fun add(field: Field) {
        field.ship = this
        fields.add(field)
    }

    fun clear() {
        fields.forEach { field -> field.ship = null }
        fields.clear()
    }

    fun show() {
        view?.visibility = View.VISIBLE
        horizontal = true
        direction = Direction.RIGHT
    }

    fun hide() {
        view?.visibility = View.INVISIBLE
    }

    fun rotate(board: Board) {
        if (fields.isEmpty() || size == 1) return

        val isFirst = direction == Direction.RIGHT || direction == Direction.BOTTOM
        val rotationField = if (isFirst) fields.first() else fields.last()
        val (row, col) = rotationField

        val startDirection = direction
        clear()

        do {
            direction = direction.next()

            val success = when (direction) {
                Direction.RIGHT -> board.set(rotationField, Field(row, col + size - 1), this)
                Direction.TOP -> board.set(Field(row - size + 1, col), rotationField, this)
                Direction.LEFT -> board.set(Field(row, col - size + 1), rotationField, this)
                Direction.BOTTOM -> board.set(rotationField, Field(row + size - 1, col), this)
            }

        } while (direction != startDirection && !success)
    }

    enum class Direction {
        RIGHT, TOP, LEFT, BOTTOM;

        fun next() : Direction {
            return when (this) {
                RIGHT -> TOP
                TOP -> LEFT
                LEFT -> BOTTOM
                else -> RIGHT
            }
        }
    }
}