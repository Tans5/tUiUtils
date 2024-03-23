package com.tans.tuiutils.systembar

import android.app.Activity
import androidx.annotation.MainThread
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.tans.tuiutils.tUiUtilsLog

@MainThread
fun Activity.fullScreenStyle(sticky: Boolean = true) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    if (sticky) {
        controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    controller.hide(WindowInsetsCompat.Type.systemBars())
    tUiUtilsLog.d(msg = "${this::class.java} fullScreenStyle success.")
}