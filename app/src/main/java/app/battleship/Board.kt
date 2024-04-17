package app.battleship

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.forEach
import androidx.core.view.get

class Board(
    size: Int,
    context: Context? = null,
    private val layout: LinearLayout? = null,
    val rightSide: Boolean = false,
    var active: Boolean = false
) {

    companion object {
        const val MIN_SIZE = 1
        const val MAX_SIZE = 20
    }

    val size: Int = size.coerceIn(MIN_SIZE, MAX_SIZE)

    private var fields: Array<Array<Field>> =
        Array(this.size) { i -> Array(this.size) { j -> Field(j, i) } }

    init {
        if (context != null && layout != null) {
            generateBoard(context, layout, rightSide)
        }
    }

    operator fun get(index: Int) : Array<Field> {
        return fields[index]
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

        setListeners(layout)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(layout: LinearLayout) {
        layout.setOnTouchListener { _, event ->
            if (!active) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    forEach { field, tv ->
                        val location = IntArray(2)
                        tv.getLocationOnScreen(location)
                        val (x, y) = location

                        if (!isInside(event.rawX, event.rawY)) {
                            field.background?.color = Color.TRANSPARENT
                        }
                        else if (event.rawX >= x && event.rawX <= x + tv.width &&
                            event.rawY >= y && event.rawY <= y + tv.height) {

                            field.background?.color = Color.DKGRAY
                        }
                        else if ((event.rawX >= x && event.rawX <= x + tv.width) ||
                            (event.rawY >= y && event.rawY <= y + tv.height)) {

                            field.background?.color = Color.LTGRAY
                        }
                        else {
                            field.background?.color = Color.TRANSPARENT
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    forEach { field, tv ->
                        field.background?.color = Color.TRANSPARENT

                        val location = IntArray(2)
                        tv.getLocationOnScreen(location)
                        val (x, y) = location

                        if (event.rawX >= x && event.rawX <= x + tv.width &&
                            event.rawY >= y && event.rawY <= y + tv.height) {

                            when (field.state) {
                                Field.State.UNKNOWN -> {
                                    field.state = Field.State.EMPTY
                                }
                                Field.State.EMPTY -> {
                                    field.state = Field.State.SHIP
                                }
                                Field.State.SHIP -> {
                                    field.state = Field.State.DESTROYED_SHIP
                                }
                                Field.State.DESTROYED_SHIP -> {
                                    field.state = Field.State.UNKNOWN
                                }
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }
}