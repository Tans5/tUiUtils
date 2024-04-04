package com.tans.tuiutils.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import androidx.annotation.FloatRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.tans.tuiutils.R
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseDialogFragment : AppCompatDialogFragment() {

    @FloatRange(from = 0.0, to = 1.0)
    open val contentViewHeightInScreenRatio: Float? = null
    @FloatRange(from = 0.0, to = 1.0)
    open val contentViewWidthInScreenRatio: Float? = null

    private var contentView: View? = null
    private val isDialogCreatedInvoked: AtomicBoolean = AtomicBoolean(false)
    abstract fun createContentView(context: Context, parent: ViewGroup): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // if restart, because configure change, force dismiss
        if (savedInstanceState != null) {
            dismissSafe()
            return
        }
    }

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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
        val dialog = createDialog(contentView)
        firstLaunchInitData()
        return dialog
    }

    open fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater =  super.onGetLayoutInflater(savedInstanceState)
        if (dialog != null && isDialogCreatedInvoked.compareAndSet(false , true)) {
            contentView?.let {
                onBindContentView(it)
            }
        }
        return layoutInflater
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    /**
     * Do data load
     */
    open fun firstLaunchInitData() {

    }

    /**
     * Do UI update.
     */
    open fun onBindContentView(view: View) {

    }

    open fun createDialog(contentView: View): Dialog {
        return requireActivity().createDefaultDialog(contentView = contentView)
    }

    fun dismissSafe() {
        val r = Runnable {
            dismiss()
        }
        if (Looper.myLooper() === Looper.getMainLooper()) {
            r.run()
        } else {
            activity?.runOnUiThread {
                r.run()
            }
        }
    }

}