package app.battleship

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get

class Board(
    size: Int,
    context: Context? = null,
    layout: LinearLayout? = null,
    leftSide: Boolean = true,
    active: Boolean = false
) {

    val size: Int

    private var fields: Array<Array<Field>>

    var tvBoard: Array<Array<TextView?>>? = null
        private set

    var active: Boolean

    init {
        this.size = size.coerceIn(1..20)
        fields = Array(this.size) { i -> Array(this.size) { j -> Field(i, j) } }

        if (context != null && layout != null) {
            generateBoard(context, layout, this.size, leftSide)
        }
        this.active = active
    }

    operator fun get(index: Int) : Array<Field> {
        return fields[index]
    }

    fun generateBoard(
        context: Context,
        layout: LinearLayout,
        dimension: Int = this.size,
        leftSide: Boolean = true
    ) {
        val size = dimension + 1
        val tvBoard = Array<Array<TextView?>>(dimension) { arrayOfNulls(dimension) }

        for (i in 0 until size) {
            val rowLayout = LinearLayout(context)
            layout.addView(rowLayout)

            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.layoutParams.width = layout.layoutParams.width
            rowLayout.layoutParams.height = layout.layoutParams.height / size
            rowLayout.gravity = Gravity.CENTER

            for (j in 0 until size) {
                val tv = TextView(context)
                if (i > 0 && j > 0) {
                    tvBoard[i-1][j-1] = tv
                }
                rowLayout.addView(tv)

                tv.layoutParams.width = rowLayout.layoutParams.width / size
                tv.layoutParams.height = rowLayout.layoutParams.height
                tv.gravity = Gravity.CENTER
                tv.setTextColor(Color.BLACK)

                if (i > 1 && j > 1) {
                    tv.background = BorderDrawable(top = false, left = false)
                }
                else if (i > 1 && j == 1) {
                    tv.background = BorderDrawable(top = false)
                }
                else if (i == 1 && j > 1) {
                    tv.background = BorderDrawable(left = false)
                }
                else if (i == 1 && j == 1) {
                    tv.background = BorderDrawable()
                }

                if (i > 0 && j > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.7f)
                }
                else if (i == 0 && j > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.5f)
                    tv.text = j.toString()
                    // tv.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                }
                else if (i > 0 && j == 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.5f)
                    tv.text = ('A' - 1 + i).toString()
                }

                // tv.setBackgroundColor(Color.rgb(i / (size - 1f), 0f, j / (size - 1f)))
            }
        }

        if (!leftSide) {
            for (child in layout.children) {
                val row = child as LinearLayout
                val view = row[0]
                row.removeViewAt(0)
                row.addView(view)
            }
        }

        this.tvBoard = tvBoard

        for (i in 0 until this.size) {
            for (j in 0 until this[i].size) {
                this[i][j].view = tvBoard[i][j]
                this[i][j].view?.text = this[i][j].state.toString()

                tvBoard[i][j]?.setOnClickListener {
                    if (active) {
                        if (this[i][j].state == Field.State.UNKNOWN) {
                            this[i][j].state = Field.State.EMPTY
                        }
                        else if (this[i][j].state == Field.State.EMPTY) {
                            this[i][j].state = Field.State.SHIP
                        }
                        else if (this[i][j].state == Field.State.SHIP) {
                            this[i][j].state = Field.State.DESTROYED_SHIP
                        }
                        else if (this[i][j].state == Field.State.DESTROYED_SHIP) {
                            this[i][j].state = Field.State.UNKNOWN
                        }
                    }
                }
            }
        }
    }
}