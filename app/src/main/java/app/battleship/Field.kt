package app.battleship

import android.widget.TextView

class Field(
    var x: Int,
    var y: Int,
    state: State = State.UNKNOWN,
    ship: Ship? = null,
    view: TextView? = null
) : Comparable<Field> {

    var view: TextView? = null

    var ship: Ship? = null

    var state: State = state
        set(value) {
            field = value
            view?.text = value.toString()
        }

    init {
        this.state = state
        this.view = view
        this.ship = ship
    }

    override fun compareTo(other: Field): Int {
        return if (x == other.x) y.compareTo(other.y) else x.compareTo(other.x)
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