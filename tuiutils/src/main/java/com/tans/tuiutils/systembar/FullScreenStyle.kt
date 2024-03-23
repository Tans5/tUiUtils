package com.tans.tuiutils.systembar

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.tans.tuiutils.tUiUtilsLog

@MainThread
fun Activity.fullScreenStyle(sticky: Boolean = true, ignoreCutoutArea: Boolean = true) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    if (sticky) {
        controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    if (ignoreCutoutArea) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
    }
    controller.hide(WindowInsetsCompat.Type.systemBars())
    tUiUtilsLog.d(msg = "${this::class.java} fullScreenStyle success.")
}