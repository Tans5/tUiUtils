package com.tans.tuiutils.systembar

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.annotation.MainThread
import androidx.core.view.WindowCompat
import com.tans.tuiutils.assertMainThread
import com.tans.tuiutils.tUiUtilsLog

enum class SystemBarThemeStyle { BySystem, Light, Dark }

@MainThread
fun Activity.systemBarThemeStyle(statusBarThemeStyle: SystemBarThemeStyle, navigationThemeStyle: SystemBarThemeStyle) {
    window.systemBarThemeStyle(this, statusBarThemeStyle, navigationThemeStyle)
    tUiUtilsLog.d(msg = "${this::class.java} systemBarThemeStyle success, statusThemeStyle=$statusBarThemeStyle, navigationThemeStyle=$navigationThemeStyle")
}

@MainThread
fun Window.systemBarThemeStyle(context: Context, statusBarThemeStyle: SystemBarThemeStyle, navigationThemeStyle: SystemBarThemeStyle) {
    assertMainThread { "systemBarThemeStyle() need invoke in main thread." }
    val isSystemDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    val lightStatus = when (statusBarThemeStyle) {
        SystemBarThemeStyle.BySystem -> !isSystemDarkMode
        SystemBarThemeStyle.Light -> true
        SystemBarThemeStyle.Dark -> false
    }

    val lightNavigation = when (navigationThemeStyle) {
        SystemBarThemeStyle.BySystem -> !isSystemDarkMode
        SystemBarThemeStyle.Light -> true
        SystemBarThemeStyle.Dark -> false
    }
    val controller =  WindowCompat.getInsetsController(this, decorView)
    controller.isAppearanceLightStatusBars = lightStatus
    controller.isAppearanceLightNavigationBars = lightNavigation
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

