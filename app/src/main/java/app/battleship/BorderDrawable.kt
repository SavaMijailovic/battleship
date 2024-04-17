package app.battleship

import android.graphics.*
import android.graphics.drawable.Drawable

class BorderDrawable(
    private val top: Boolean = true,
    private val right: Boolean = true,
    private val bottom: Boolean = true,
    private val left: Boolean = true,
    private val borderWidth: Float = 5.0f,
    private val borderColor: Int = Color.BLACK,
    private val borderStyle: Paint.Style = Paint.Style.STROKE,
    color: Int = Color.TRANSPARENT,
) : Drawable() {

    var color: Int = color
        set(value) {
            field = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        val borderPaint = Paint().apply {
            strokeWidth = borderWidth
            color = borderColor
            style = borderStyle
        }

        val colorPaint = Paint().apply {
            style = Paint.Style.FILL
            this.color = this@BorderDrawable.color
        }

        val rect = RectF(bounds)
        canvas.drawRect(rect, colorPaint)

        if (top) {
            canvas.drawLine(rect.left, rect.top, rect.right, rect.top, borderPaint)
        }
        if (right) {
            canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, borderPaint)
        }
        if (bottom) {
            canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, borderPaint)
        }
        if (left) {
            canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, borderPaint)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int { return PixelFormat.OPAQUE }
}