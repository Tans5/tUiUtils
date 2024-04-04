package com.tans.tuiutils.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import com.tans.tuiutils.R
import android.view.WindowManager.LayoutParams

fun Activity.createDefaultDialog(
    contentView: View,
    isCancelable: Boolean = true,
    dimAmount: Float = 0.4f,
    @StyleRes
    defaultTheme: Int = R.style.tUiUtils_BaseDialog,
    windowGravity: Int = Gravity.CENTER,
    @StyleRes
    windowAnima: Int = if (windowGravity == Gravity.BOTTOM) R.style.tUiDefaultBottomDialogAnima else R.style.tUiDefaultCenterDialogAnima,
    windowWidth: Int = LayoutParams.WRAP_CONTENT,
    windowHeight: Int = LayoutParams.WRAP_CONTENT,
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
    dialog.apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(contentView)
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
        setGravity(windowGravity)
        setLayout(windowWidth, windowHeight)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setWindowAnimations(windowAnima)
    }
    return dialog
}