package com.example.dimmer

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent

/** Shows or hides a black overlay over the whole screen when the accessibility button is tapped. */
class DimService : AccessibilityService() {

    private val windowManager by lazy { getSystemService(WindowManager::class.java) }
    private var overlay: View? = null

    override fun onServiceConnected() {
        accessibilityButtonController.registerAccessibilityButtonCallback(
            object : AccessibilityButtonController.AccessibilityButtonCallback() {
                override fun onClicked(controller: AccessibilityButtonController) = toggle()
            })
    }

    private fun toggle() {
        val current = overlay
        if (current != null) {
            windowManager.removeView(current)
            overlay = null
        } else {
            val view = View(this).apply {
                setBackgroundColor(Color.BLACK)
                alpha = dimPercent(this@DimService) / 100f
            }
            windowManager.addView(view, overlayParams())
            overlay = view
        }
    }

    // Accessibility overlays are trusted windows, so touches pass through at any opacity.
    private fun overlayParams() = LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT,
        LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        LayoutParams.FLAG_NOT_TOUCHABLE or LayoutParams.FLAG_NOT_FOCUSABLE or
            LayoutParams.FLAG_LAYOUT_IN_SCREEN or LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        // Cover the status bar, navigation bar and camera cutout too.
        fitInsetsTypes = 0
        layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    }

    override fun onDestroy() {
        overlay?.let { windowManager.removeView(it) }
        overlay = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    companion object {
        private const val PREFS = "settings"
        private const val KEY_DIM_PERCENT = "dim_percent"
        private const val DEFAULT_DIM_PERCENT = 80

        fun prefs(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        fun dimPercent(context: Context) = prefs(context).getInt(KEY_DIM_PERCENT, DEFAULT_DIM_PERCENT)

        fun setDimPercent(context: Context, percent: Int) =
            prefs(context).edit().putInt(KEY_DIM_PERCENT, percent).apply()
    }
}
