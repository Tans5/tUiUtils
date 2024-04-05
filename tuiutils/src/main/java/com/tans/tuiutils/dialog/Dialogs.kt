package com.tans.tuiutils.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Layout
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import com.tans.tuiutils.R
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.android.material.sidesheet.SideSheetDialog
import com.tans.tuiutils.systembar.SystemBarThemeStyle
import com.tans.tuiutils.systembar.systemBarThemeStyle

fun Activity.createDefaultDialog(
    contentView: View,
    isCancelable: Boolean = true,
    dimAmount: Float = 0.6f,
    @ColorInt
    navigationBarColor: Int = Color.TRANSPARENT,
    @StyleRes
    defaultTheme: Int = R.style.tUiUtils_BaseDialog,
    @StyleRes
    windowAnima: Int = R.style.tUiDefaultCenterDialogAnima,
    onTouchEvent: (event: MotionEvent) -> Boolean = { false },
): Dialog {
    val dialog = object : AppCompatDialog(this, defaultTheme) {
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val hook = onTouchEvent(event)
            return if (!hook) {
                super.onTouchEvent(event)
            } else {
                true
            }
        }
    }
    val wrapper = FrameLayout(this)
    val wrapperLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    wrapper.layoutParams = wrapperLayoutParams
    wrapper.addView(contentView)
    if (isCancelable) {
        wrapper.setOnClickListener {
            dialog.dismiss()
        }
    }
    dialog.apply {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(wrapper)
        setCanceledOnTouchOutside(isCancelable)
        setCancelable(isCancelable)
    }
    dialog.window?.apply {
        if (dimAmount < 0.01f) {
            clearFlags(LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.0f)
        } else {
            addFlags(LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(dimAmount)
        }
        addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setWindowAnimations(windowAnima)
        this.navigationBarColor = navigationBarColor
    }
    return dialog
}

fun Activity.createBottomSheetDialog(
    contentView: View,
    isCancelable: Boolean = true,
    dimAmount: Float = 0.6f,
    statusBarThemeStyle: SystemBarThemeStyle = SystemBarThemeStyle.BySystem,
    navigationThemeStyle: SystemBarThemeStyle = SystemBarThemeStyle.BySystem,
    @ColorInt
    navigationBarColor: Int = Color.TRANSPARENT,
    behaviorCallback: (behavior: BottomSheetBehavior<*>) -> Unit = {}
): Dialog {
    val d = BottomSheetDialog(this)
    d.apply {
        setContentView(contentView)
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(isCancelable)
        behaviorCallback(behavior)
    }
    d.window?.apply {
        if (dimAmount < 0.01f) {
            clearFlags(LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.0f)
        } else {
            addFlags(LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(dimAmount)
        }
        this.navigationBarColor = navigationBarColor
        this.systemBarThemeStyle(this@createBottomSheetDialog, statusBarThemeStyle, navigationThemeStyle)
    }
    return d
}

fun Activity.createSideSheetDialog(
    contentView: View,
    isCancelable: Boolean = true,
    dimAmount: Float = 0.6f,
    statusBarThemeStyle: SystemBarThemeStyle = SystemBarThemeStyle.BySystem,
    navigationThemeStyle: SystemBarThemeStyle = SystemBarThemeStyle.BySystem,
    @ColorInt
    navigationBarColor: Int = Color.TRANSPARENT,
    direction: Int = SideSheetBehavior.EDGE_LEFT,
    behaviorCallback: (behavior: SideSheetBehavior<*>) -> Unit = {}): Dialog {
    val d = SideSheetDialog(this)
    d.apply {
        setContentView(contentView)
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(isCancelable)
        setSheetEdge(direction)
        behaviorCallback(d.behavior)
    }
    d.window?.apply {
        if (dimAmount < 0.01f) {
            clearFlags(LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.0f)
        } else {
            addFlags(LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(dimAmount)
        }
        this.navigationBarColor = navigationBarColor
        this.systemBarThemeStyle(this@createSideSheetDialog, statusBarThemeStyle, navigationThemeStyle)
    }
    return d
}