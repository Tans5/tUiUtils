package com.tans.tuiutils.systembar

import android.app.Activity
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.annotation.MainThread
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tans.tuiutils.tUiUtilsLog

@MainThread
fun Activity.fitSystemWindow() {
    val viewTreeObserver = window.decorView.viewTreeObserver
    viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val contentView = window.decorView.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)
            if (contentView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(contentView) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
                window.decorView.requestApplyInsets()
                tUiUtilsLog.d(msg = "${this::class.java} set fitSystemWindow success.")
            } else {
                tUiUtilsLog.e(msg = "${this::class.java} set fitSystemWindow fail, contentView is null.")
            }
            window.decorView.viewTreeObserver.removeOnPreDrawListener(this)
            return true
        }
    })
}