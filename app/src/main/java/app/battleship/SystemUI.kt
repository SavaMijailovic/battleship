package app.battleship

import android.content.res.Resources
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun hideSystemUI(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window,
        window.decorView.findViewById(android.R.id.content)).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun showSystemUI(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(window,
        window.decorView.findViewById(android.R.id.content)).let { controller ->
        controller.show(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

private const val UNIT_WIDTH = 28
private const val UNIT_HEIGHT = 14

fun getUnitSize(resources: Resources, unitWidth: Int = UNIT_WIDTH, unitHeight: Int = UNIT_HEIGHT) : Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    val navBarHeight = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    val width = resources.displayMetrics.widthPixels / unitWidth
    val height = (resources.displayMetrics.heightPixels + navBarHeight) / unitHeight
    return if (width < height) width else height
}