package app.battleship

import android.widget.LinearLayout
import java.util.TreeSet

class Ship(val length: Int, view: LinearLayout? = null) {

    var health: Int = 0
        set(value) { field = value.coerceAtLeast(0) }

    var fileds: TreeSet<Field>? = null

    var placement : Boolean = false

    var view: LinearLayout?

    init {
        health = length
        this.view = view
    }
}