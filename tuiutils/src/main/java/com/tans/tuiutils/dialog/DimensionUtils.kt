package com.tans.tuiutils.dialog

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.core.content.getSystemService

@Suppress("DEPRECATION")
fun Context.getDisplaySize(): Pair<Int, Int> {
    val vm = getSystemService<WindowManager>()!!
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = vm.currentWindowMetrics.bounds
        bounds.width() to bounds.height()
    } else {
        val point = Point()
        vm.defaultDisplay?.getSize(point)
        point.x to point.y
    }
}