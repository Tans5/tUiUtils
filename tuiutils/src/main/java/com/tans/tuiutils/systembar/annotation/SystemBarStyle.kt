package com.tans.tuiutils.systembar.annotation


/**
 * ColorString format is "#RRGGBB" or "#AARRGGBB"
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SystemBarStyle(
    val statusBarLightColor: String = TRANSPARENT_COLOR_STR,
    val statusBarDarkColor: String = TRANSPARENT_COLOR_STR,
    val lightStatusBar: Boolean = true,
    val navigationBarLightColor: String = TRANSPARENT_COLOR_STR,
    val navigationBarDarkColor: String = TRANSPARENT_COLOR_STR,
    val lightNavigationBar: Boolean = true
) {
    companion object {
        const val TRANSPARENT_COLOR_STR = "#00FFFFFF"
    }
}
