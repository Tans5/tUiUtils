package com.tans.tuiutils.systembar

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.tans.tuiutils.systembar.annotation.FitSystemWindow
import com.tans.tuiutils.systembar.annotation.FullScreenStyle
import com.tans.tuiutils.systembar.annotation.SystemBarStyle
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.atomic.AtomicBoolean

object AutoApplySystemBarAnnotation : ActivityLifecycleCallbacks {

    private val hasInited: AtomicBoolean = AtomicBoolean(false)

    @JvmStatic
    fun init(application: Application) {
        if (hasInited.compareAndSet(false, true)) {
            tUiUtilsLog.d(msg = "AutoApplySystemBarAnnotation init.")
            application.registerActivityLifecycleCallbacks(this)

        } else {
            tUiUtilsLog.w(msg = "AutoApplySystemBarAnnotation has already inited.")
        }
    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val allAnnotations = activity::class.java.declaredAnnotations
        val systemBarStyle = allAnnotations.filterIsInstance<SystemBarStyle>().getOrNull(0)
        if (systemBarStyle != null) {
            tUiUtilsLog.d(msg = "${activity::class.java} found SystemBarStyle annotation.")
            if (activity is ComponentActivity) {
                activity.systemBarColor(
                    statusBarLightColor = Color.parseColor(systemBarStyle.statusBarLightColor),
                    statusBarDarkColor = Color.parseColor(systemBarStyle.statusBarDarkColor),
                    navigationBarLightColor = Color.parseColor(systemBarStyle.navigationBarLightColor),
                    navigationBarDarkColor = Color.parseColor(systemBarStyle.navigationBarDarkColor)
                )
            } else {
                tUiUtilsLog.w(msg = "${activity::class.java} is not ComponentActivity, can't set system bar color.")
            }
            activity.systemBarThemeStyle(
                statusBarThemeStyle = SystemBarThemeStyle.entries.getOrNull(systemBarStyle.statusBarThemeStyle) ?: SystemBarThemeStyle.BySystem,
                navigationThemeStyle = SystemBarThemeStyle.entries.getOrNull(systemBarStyle.navigationBarThemeStyle) ?: SystemBarThemeStyle.BySystem
            )
        }
        val fullScreenStyle = allAnnotations.filterIsInstance<FullScreenStyle>().getOrNull(0)
        if (fullScreenStyle != null) {
            tUiUtilsLog.d(msg = "${activity::class.java} found FullScreenStyle annotation.")
            activity.fullScreenStyle(sticky = fullScreenStyle.sticky, ignoreCutoutArea = fullScreenStyle.ignoreCutoutArea)
        }

        val fitSystemWindowStyle = allAnnotations.filterIsInstance<FitSystemWindow>().getOrNull(0)
        if (fitSystemWindowStyle != null) {
            tUiUtilsLog.d(msg = "${activity::class.java} found FitSystemWindow annotation.")
            activity.fitSystemWindow(fitSystemWindow = fitSystemWindowStyle.fitSystemWindow)
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}