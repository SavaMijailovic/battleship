package app.battleship

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.forEach
import androidx.core.view.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class Board(
    size: Int = DIMENSION,
    context: Context? = null,
    val layout: LinearLayout? = null,
    private val rightSide: Boolean = false,
    var active: Boolean = false,
    random: Boolean = false,
) {

    companion object {
        const val MIN_SIZE = 1
        const val MAX_SIZE = 20
        const val DIMENSION = 10
    }

    val size: Int = size.coerceIn(MIN_SIZE, MAX_SIZE)

    private var fields: Array<Array<Field>> =
        Array(this.size) { i -> Array(this.size) { j -> Field(i, j) } }

    init {
        if (context != null && layout != null) {
            generateBoard(context, layout, rightSide)
        }
        if (random) {
            randomPlacement()
        }
    }

    operator fun get(index: Int) : Array<Field> {
        return fields[index]
    }

    operator fun get(field: Field) : Field {
        return this[field.row][field.col]
    }

    @SuppressLint("SetTextI18n")
    fun generateBoard(
        context: Context,
        layout: LinearLayout,
        rightSide: Boolean = false
    ) {
        val dimension = this.size
        val size = dimension + 1
        val tvBoard = Array<Array<TextView?>>(dimension) { arrayOfNulls(dimension) }

        for (i in 0 until size) {
            val rowLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    layout.layoutParams.width,
                    layout.layoutParams.height / size
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            layout.addView(rowLayout)

            for (j in 0 until size) {
                val tv = TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        rowLayout.layoutParams.width / size,
                        rowLayout.layoutParams.height
                    )
                    gravity = Gravity.CENTER
                    setTextColor(Color.BLACK)
                }
                rowLayout.addView(tv)

                if (i > 0 && j > 0) {
                    tvBoard[i-1][j-1] = tv
                }

                tv.background = when {
                    i > 1 && j > 1 -> BorderDrawable(top = false, left = false)
                    i > 1 && j == 1 -> BorderDrawable(top = false)
                    i == 1 && j > 1 -> BorderDrawable(left = false)
                    i == 1 && j == 1 -> BorderDrawable()
                    else -> tv.background
                }

                val height = tv.layoutParams.height
                if (i > 0 && j > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, height * 0.7f)
                }
                else if (i == 0 && j > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, height * 0.5f)
                    tv.text = j.toString()
                }
                else if (i > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, height * 0.5f)
                    tv.text = ('A' - 1 + i).toString()
                }
            }
        }

        if (rightSide) {
            layout.forEach { child ->
                val row = child as LinearLayout
                val view = row[0]
                row.removeViewAt(0)
                row.addView(view)
            }
        }

        forEachIndexed { i, j, field ->
            field.apply {
                view = tvBoard[i][j]
                background = tvBoard[i][j]?.background as BorderDrawable
                view?.text = this@Board[i][j].state.toString()
            }
        }
    }

    fun isAvailable(top: Int, bottom: Int, left: Int, right: Int) : Boolean {
        for (i in coerceIn(top)..coerceIn(bottom)) {
            for (j in coerceIn(left)..coerceIn(right)) {
                if (this[i][j].isShip()) {
                    return false
                }
            }
        }
        return true
    }

    fun coerceIn(index: Int) : Int {
        return index.coerceIn(0, size - 1)
    }

    fun isInside(index: Int) : Boolean {
        return index in 0 until size
    }

    fun isInside(field: Field) : Boolean {
        return isInside(field.row) && isInside(field.col)
    }
    
    fun isInside(x: Float, y: Float) : Boolean {
        if (layout == null) return false
        val width = this[0][0].view?.width ?: 0
        val height = this[0][0].view?.height ?: 0
        
        val layoutLocation = IntArray(2)
        layout.getLocationOnScreen(layoutLocation)
        val (layoutX, layoutY) = layoutLocation
        
        return Rect(
            layoutX + if (rightSide) 0 else width,
            layoutY + height,
            layoutX + layout.width - if (rightSide) width else 0,
            layoutY + layout.height
        ).contains(x.toInt(), y.toInt())
    }

    inline fun forEach(action: (field: Field) -> Unit) {
        forEachIndexed { _, _, field ->
            action(field)
        }
    }

    inline fun forEachIndexed(action: (i: Int, j: Int, field: Field) -> Unit) {
        for (i in 0 until this.size) {
            for (j in 0 until this.size) {
                action(i, j, this[i][j])
            }
        }
    }

    inline fun forEach(action: (field: Field, view: TextView) -> Unit) {
        forEachIndexed { _, _, field, view ->
            action(field, view)
        }
    }

    inline fun forEachIndexed(action: (i: Int, j: Int, field: Field, view: TextView) -> Unit) {
        for (i in 0 until this.size) {
            for (j in 0 until this.size) {
                if (this[i][j].view != null) {
                    action(i, j, this[i][j], this[i][j].view as TextView)
                }
            }
        }
    }

    fun update(target: Field) {
        val field = this[target]

        CoroutineScope(Dispatchers.Main).launch {
            field.state = when (target.state) {
                Field.State.UNKNOWN -> Field.State.EMPTY
                Field.State.SHIP -> Field.State.DESTROYED_SHIP
                else -> field.state
            }

            if (target.state == Field.State.SHIP) {
                updateIfDestroyed(target.ship)
            }
        }
    }

    private fun updateIfDestroyed(ship: Ship?) {
        if (ship == null || ship.health != 0) {
            return
        }

        val (top, bottom) = Pair(ship.fields.first().row - 1, ship.fields.last().row + 1)
        val (left, right) = Pair(ship.fields.first().col - 1, ship.fields.last().col + 1)

        for (row in coerceIn(top)  .. coerceIn(bottom)) {
            for (col in coerceIn(left) .. coerceIn(right)) {
                val field = this[row][col]
                if (field.state == Field.State.UNKNOWN) {
                    field.state = Field.State.EMPTY
                }
                else if (field.state == Field.State.DESTROYED_SHIP) {
                    field.state = Field.State.SHIP
                }
            }
        }
    }

    fun set(start: Field, end: Field, ship: Ship,
            action: (Field, Ship) -> Unit = { f, s -> s.add(f) }) : Boolean {

        if (!isInside(start) || !isInside(end)) {
            return false
        }

        if (!isAvailable(start.row - 1, end.row + 1, start.col - 1, end.col + 1)) {
            return false
        }

        if (start.row == end.row) {
            ship.horizontal = true
            for (col in start.col .. end.col) {
                action(this[start.row][col], ship)
            }
        }
        else if (start.col == end.col) {
            ship.horizontal = false
            for (row in start.row .. end.row) {
                action(this[row][start.col], ship)
            }
        }
        else {
            return false
        }
        return true
    }

    private fun generateShips() : List<Ship> {
        val maxShipSize = 4; val minShipSize = 1
        val ships = mutableListOf<Ship>()
        for (shipSize in maxShipSize downTo minShipSize) {
            for (n in 0 until 5 - shipSize) {
                ships.add(Ship(shipSize))
            }
        }
        return ships
    }

    private fun randomPlacement() = randomPlacement(generateShips())

    fun randomPlacement(ships: List<Ship>) {
        val list: MutableList<Pair<Int, Int>> = mutableListOf()
        for (i in 0 until size) {
            for (j in 0 until size) {
                list.add(Pair(i, j))
            }
        }

        ships.forEach { ship ->
            ship.clear()
            ship.hide()

            while (true) {
                val (row, col) = list.random()

                val start = Field(row, col)

                val end = if (Random.nextBoolean()) {
                    Field(row, col + ship.size - 1)
                }
                else {
                    Field(row + ship.size - 1, col)
                }

                if (set(start, end, ship)) {
                    for (i in coerceIn(start.row - 1) .. coerceIn(end.row + 1)) {
                        for (j in coerceIn(start.col - 1) .. coerceIn(end.col + 1)) {
                            list.remove(Pair(i, j))
                        }
                    }
                    break
                }
            }
        }
    }
}