package com.tans.tuiutils.systembar.annotation


/**
 * ColorString format is "#RRGGBB" or "#AARRGGBB"
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SystemBarStyle(
    val statusBarLightColor: String = TRANSPARENT_COLOR_STR,
    val statusBarDarkColor: String = TRANSPARENT_COLOR_STR,
    val statusBarThemeStyle: Int = 0, // 0: by system, 1: light, 2: dark
    val navigationBarLightColor: String = TRANSPARENT_COLOR_STR,
    val navigationBarDarkColor: String = TRANSPARENT_COLOR_STR,
    val navigationBarThemeStyle: Int = 0 // 0: by system, 1: light, 2: dark
) {
    companion object {
        const val TRANSPARENT_COLOR_STR = "#00FFFFFF"
    }
}
