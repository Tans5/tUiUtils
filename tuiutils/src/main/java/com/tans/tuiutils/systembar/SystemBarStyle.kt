package com.tans.tuiutils.systembar

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.annotation.MainThread
import androidx.core.view.WindowCompat
import com.tans.tuiutils.tUiUtilsLog

@MainThread
fun Activity.lightSystemBar(lightStatusBar: Boolean, lightNavigationBar: Boolean) {
    window.lightSystemBar(lightStatusBar, lightNavigationBar)
    tUiUtilsLog.d(msg = "${this::class.java} lightSystemBar success, lightStatusBar=$lightStatusBar, lightNavigationBar=$lightNavigationBar")
}

@MainThread
fun Window.lightSystemBar(lightStatusBar: Boolean, lightNavigationBar: Boolean) {
    val controller =  WindowCompat.getInsetsController(this, decorView)
    controller.isAppearanceLightStatusBars = lightStatusBar
    controller.isAppearanceLightNavigationBars = lightNavigationBar
}

@MainThread
fun ComponentActivity.systemBarColor(
    @ColorInt
    statusBarLightColor: Int = Color.TRANSPARENT,
    @ColorInt
    statusBarDarkColor: Int = Color.TRANSPARENT,
    @ColorInt
    navigationBarLightColor: Int = Color.TRANSPARENT,
    @ColorInt
    navigationBarDarkColor: Int = Color.TRANSPARENT,
) {

    val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    if (isDarkMode) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(statusBarDarkColor),
            navigationBarStyle = SystemBarStyle.dark(navigationBarDarkColor)
        )
    } else {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(statusBarLightColor, statusBarDarkColor),
            navigationBarStyle = SystemBarStyle.light(navigationBarLightColor, navigationBarDarkColor)
        )
    }
    tUiUtilsLog.d(msg = "${this::class.java} systemBarColor success, isDarkMode=$isDarkMode")
}

