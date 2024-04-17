package app.battleship

import android.widget.TextView
import java.util.Objects
import kotlin.math.abs

class Field(
    var x: Int,
    var y: Int,
    state: State = State.UNKNOWN,
    var view: TextView? = null,
    var ship: Ship? = null
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

    init {
        this.state = state
    }

    override fun compareTo(other: Field): Int {
        return if (x == other.x) y.compareTo(other.y) else x.compareTo(other.x)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Field) {
            return false
        }
        return this.compareTo(other) == 0
    }

    override fun hashCode(): Int {
        return Objects.hash(x, y)
    }

    fun distanceTo(other: Field): Int {
        return abs(x - other.x) + abs(y - other.y)
    }

    override fun toString(): String {
        return "($x, $y)"
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