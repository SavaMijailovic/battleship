package app.battleship

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.*
import kotlin.math.max
import kotlin.math.min

class ShipsPlacementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ships_placement)
        setBarsBehavior(window)

        val layout = findViewById<LinearLayout>(R.id.layout1)
        var size = min(resources.displayMetrics.heightPixels, resources.displayMetrics.widthPixels).toFloat()
        size *= 0.8f
        layout.layoutParams.width = size.toInt()
        layout.layoutParams.height = size.toInt()

        val dimension = 10

        generateBoard(layout, dimension)
        // generateBoard(layout, dimension, leftSide = false)
    }

    private fun setBarsBehavior(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun generateBoard(
        layout: LinearLayout,
        dimension: Int,
        leftSide: Boolean = true,
        borderWidth: Float = 4f,
        borderColor: Int = Color.BLACK
    ) : Array<Array<TextView?>> {

        val size = max(min(dimension, 20), 1) + 1

        layout.layoutParams.width = layout.layoutParams.width / size * size
        layout.layoutParams.height = layout.layoutParams.height / size * size

        // val tvBoard = Array(size) { Array(size) { TextView(this) } }
        val tvBoard = Array<Array<TextView?>>(size) { arrayOfNulls(size) }

        for (i in 0 until size) {

            val rowLayout = LinearLayout(this)
            layout.addView(rowLayout)

            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.layoutParams = LinearLayout.LayoutParams(
                layout.layoutParams.width,
                layout.layoutParams.height / size
            )
            rowLayout.gravity = Gravity.CENTER

            for (j in 0 until size) {

                val tv = TextView(this)
                if (i > 0 && j > 0) {
                    tvBoard[i-1][j-1] = tv
                }
                rowLayout.addView(tv)

                tv.layoutParams.width = rowLayout.layoutParams.width / size
                tv.layoutParams.height = rowLayout.layoutParams.height
                tv.gravity = Gravity.CENTER
                tv.setTextColor(Color.BLACK)

                if (i > 1 && j > 1) {
                    tv.background = BorderDrawable(false, true, true, false, borderWidth, borderColor)
                }
                else if (i > 1 && j == 1) {
                    tv.background = BorderDrawable(false, true, true, true, borderWidth, borderColor)
                }
                else if (i == 1 && j > 1) {
                    tv.background = BorderDrawable(true, true, true, false, borderWidth, borderColor)
                }
                else if (i == 1 && j == 1) {
                    tv.background = BorderDrawable(true, true, true, true, borderWidth, borderColor)
                }


                if (i > 0 && j > 0) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.layoutParams.height * 0.7f)

                    tv.setOnClickListener {
                        if (tv.text.isEmpty()) {
                            tv.text = "O"
                        } else {
                            tv.text = ""
                        }
                    }
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

        return tvBoard
    }

    private class BorderDrawable(
        private val top: Boolean,
        private val right: Boolean,
        private val bottom: Boolean,
        private val left: Boolean,
        private val borderWidth: Float = 4.0f,
        private val borderColor: Int = Color.BLACK,
        private val borderStyle: Paint.Style = Paint.Style.STROKE
    ) : Drawable() {

        override fun draw(canvas: Canvas) {
            val paint = Paint().apply {
                color = borderColor
                style = borderStyle
                strokeWidth = borderWidth
            }

            val rect = RectF(bounds)

            if (top) {
                canvas.drawLine(rect.left, rect.top, rect.right, rect.top, paint) // Top
            }
            if (right) {
                canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint) // Right
            }
            if (bottom) {
                canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint) // Bottom
            }
            if (left) {
                canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint) // Left
            }
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"))
        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }
    }
}