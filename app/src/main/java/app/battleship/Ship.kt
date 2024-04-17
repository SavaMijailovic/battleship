package app.battleship

import android.view.View
import android.widget.LinearLayout
import java.util.TreeSet

class Ship(val size: Int, var view: LinearLayout? = null) {

    var health: Int = 0
        set(value) { field = value.coerceAtLeast(0) }

    var fields: TreeSet<Field> = TreeSet()

    var horizontal: Boolean = true

    init {
        health = size
    }

    fun add(field: Field) {
        field.apply {
            state = Field.State.SHIP
            ship = this@Ship
        }
        fields.add(field)
    }

    fun clear() {
        fields.forEach { field ->
            field.state = Field.State.UNKNOWN
            field.ship = null
        }
        fields.clear()
    }

    fun show() {
        view?.visibility = View.VISIBLE
        horizontal = true
    }

    fun hide() {
        view?.visibility = View.INVISIBLE
    }

    fun rotate(board: Board) {
        if (fields.isEmpty() || size == 1) {
            return
        }

        val rotationField = if (horizontal) fields.first() else fields.last()
        val (x, y) = Pair(rotationField.x, rotationField.y)

        if (horizontal) {
            val start = y - size + 1

            if (!board.isInside(start)) {
                return
            }
            if (!board.isAvailable(start - 1, y - 1, x - 1, x + 1)) {
                return
            }

            clear()
            for (i in start .. y) {
                add(board[i][x])
            }
            horizontal = false
        }
        else {
            val end = x + size - 1

            if (!board.isInside(end)) {
                return
            }
            if (!board.isAvailable(y + 1, y - 1, x + 1, end + 1)) {
                return
            }

            clear()
            for (j in x .. end) {
                add(board[y][j])
            }
            horizontal = true
        }
    }
}