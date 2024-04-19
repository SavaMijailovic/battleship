package app.battleship

import android.view.View
import android.widget.LinearLayout
import java.util.TreeSet

class Ship(val size: Int, var view: LinearLayout? = null) {

    var health: Int = 0
        set(value) { field = value.coerceAtLeast(0) }

    // koristimo kako bi prolazili kroz svaki razliciti smer nakon klika
    var direction : Int = 0

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

        // svaki smer posmatramo kao broj od 0 do 3
        val mod = direction % 4

        direction += 1

        val directionField = if (!horizontal) fields.last() else fields.first()
        val (x,y) = Pair(directionField.x,directionField.y)


        // TODO: implementirati tako da ukoliko ne nadjemo odgovarajuc smer nakon jednog klika, program nastavi da trazi narednu mogucu rotaciju

        if (mod == 0) {
            val edge = y - size + 1

            if (!board.isInside(edge)){
                return
            }

            if (!board.isAvailable(edge - 1, y - 1, x - 1, x + 1)) {
                return
            }

            clear()
            for (i in edge .. y) {
                add(board[i][x])
            }

            horizontal = false
        }

        if (mod == 1) {
            val edge = x - size + 1

            if (!board.isInside(edge)){
                return
            }

            if (!board.isAvailable(y - 1, y + 1, edge - 1, x - 1)) {
                return
            }

            clear()
            for (i in edge .. x) {
                add(board[y][i])
            }

            horizontal = false
        }

        if (mod == 2) {
            val edge = y + size - 1

            if (!board.isInside(edge)){
                return
            }

            if (!board.isAvailable(y + 1, edge + 1, x - 1, x + 1)) {
                return
            }

            clear()
            for (i in y .. edge) {
                add(board[i][x])
            }

            horizontal = true

        }

        if (mod == 3) {
            val edge = x + size - 1

            if (!board.isInside(edge)){
                return
            }

            if (!board.isAvailable(y - 1, y + 1, x + 1, edge + 1))  {
                return
            }

            clear()
            for (j in x .. edge) {
                add(board[y][j])
            }

            horizontal = true
        }

    }
}