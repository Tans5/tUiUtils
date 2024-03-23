package com.tans.tuiutils.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import androidx.annotation.FloatRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.tans.tuiutils.R
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseDialogFragment : AppCompatDialogFragment() {

    open val dimAmount: Float = 0.4f
    open val isCanceledOnTouchOutside: Boolean = true
    open val gravity = Gravity.CENTER
    open val windowWith: Int = WindowManager.LayoutParams.WRAP_CONTENT
    open val windowHeight: Int = WindowManager.LayoutParams.WRAP_CONTENT

    @FloatRange(from = 0.0, to = 1.0)
    open val contentViewHeightInScreenRatio: Float? = null
    @FloatRange(from = 0.0, to = 1.0)
    open val contentViewWidthInScreenRatio: Float? = null

    @StyleRes
    open val defaultTheme: Int = R.style.tUiUtils_BaseDialog

    @get:StyleRes
    open val defaultDialogAnima: Int get() {
        val g = gravity
        return if (g == Gravity.BOTTOM) R.style.tUiDefaultButtonDialogAnima else R.style.tUiDefaultCenterDialogAnima
    }

    private var contentView: View? = null
    private val isDialogCreatedInvoked: AtomicBoolean = AtomicBoolean(false)
    abstract fun createContentView(context: Context, parent: ViewGroup): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
            return
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState != null) {
            return super.onCreateDialog(savedInstanceState)
        }
        val activity = activity ?: return super.onCreateDialog(null)

        val contentView = createContentView(context = activity, parent = activity.window.decorView as ViewGroup)
        val widthRatio = contentViewWidthInScreenRatio
        val heightRatio = contentViewHeightInScreenRatio
        if (widthRatio != null || heightRatio != null) {
            val (maxWidth, maxHeight) = activity.getDisplaySize()
            val lp = contentView.layoutParams ?: ViewGroup.LayoutParams(0, 0)
            if (widthRatio != null) {
                lp.width = (widthRatio * maxWidth + 0.5f).toInt()
            }
            if (heightRatio != null) {
                lp.height = (heightRatio * maxHeight + 0.5f).toInt()
            }
            contentView.layoutParams = lp
        }
        this.contentView = contentView

        val dialog = object : AppCompatDialog(activity, defaultTheme) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                val hook = this@BaseDialogFragment.onTouchEvent(event)
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
            setCanceledOnTouchOutside(isCanceledOnTouchOutside)
        }

        dialog.window?.apply {
            if (dimAmount < 0.01f) {
                clearFlags(FLAG_DIM_BEHIND)
                setDimAmount(0.0f)
            } else {
                addFlags(FLAG_DIM_BEHIND)
                setDimAmount(dimAmount)
            }
            setGravity(gravity)
            setLayout(windowWith, windowHeight)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            setWindowAnimations(defaultDialogAnima)
        }

        return dialog
    }

    open fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater =  super.onGetLayoutInflater(savedInstanceState)
        if (dialog != null && isDialogCreatedInvoked.compareAndSet(false , true)) {
            contentView?.let {
                onDialogCreated(it)
            }
        }
        return layoutInflater
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    open fun onDialogCreated(view: View) {

    }


}