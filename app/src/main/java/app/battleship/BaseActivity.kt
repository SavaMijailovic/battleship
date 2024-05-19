package app.battleship

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

open class BaseActivity(private val activityLayout: Int) : AppCompatActivity() {

    constructor() : this(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityLayout)
        hideSystemUI()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        resize()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    protected open fun resize() {}

    private companion object {
        const val UNIT_WIDTH = 28
        const val UNIT_HEIGHT = 14
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    protected fun getUnitSize(unitWidth: Int = UNIT_WIDTH, unitHeight: Int = UNIT_HEIGHT) : Int {
        val resourceId = resources.getIdentifier("navigation_bar_height_landscape", "dimen", "android")
        val navBarHeight = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        val width = resources.displayMetrics.widthPixels / unitWidth
        val height = (resources.displayMetrics.heightPixels + navBarHeight) / unitHeight
        return if (width < height) width else height
    }
}