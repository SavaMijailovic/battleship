package app.battleship

import android.widget.TextView
import java.util.Objects
import kotlin.math.abs

class Field(
    var row: Int,
    var col: Int,
    state: State = State.UNKNOWN,
    ship: Ship? = null,
    var view: TextView? = null,
) : Comparable<Field> {

    var state: State = state
        set(value) {
            field = value
            view?.text = value.toString()
        }

    var background: BorderDrawable? = null
        set(value) {
            field = value
            view?.background = value
        }

    var ship: Ship? = null
        set(value) {
            field = value
            state = if (field != null) State.SHIP else State.UNKNOWN
        }

    init {
        this.state = state
        this.ship = ship
    }

    constructor() : this(0, 0)

    operator fun component1() : Int = row
    operator fun component2() : Int = col

    override fun compareTo(other: Field) : Int {
        return if (col == other.col) row.compareTo(other.row) else col.compareTo(other.col)
    }

    override fun equals(other: Any?) : Boolean {
        if (this === other) return true
        if (other !is Field) return false
        return this.compareTo(other) == 0
    }

    override fun hashCode() : Int {
        return Objects.hash(row, col)
    }

    fun distanceTo(other: Field) : Int {
        return abs(row - other.row) + abs(col - other.col)
    }

    override fun toString() : String {
        return "$row $col"
    }

    fun isShip() : Boolean {
        return state == State.SHIP && ship != null
    }

    enum class State {
        UNKNOWN, EMPTY, SHIP, DESTROYED_SHIP;

        override fun toString(): String {
            return when (this) {
                UNKNOWN -> ""
                EMPTY -> "❌"
                SHIP -> "\uD83D\uDEA2"
                DESTROYED_SHIP -> "\uD83D\uDCA5"
            }
        }
    }
}