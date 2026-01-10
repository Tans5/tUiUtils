package com.tans.tuiutils.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.tans.tuiutils.activity.IContentViewCreator
import com.tans.tuiutils.activity.tryCreateNewContentView
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseDialogFragment : AppCompatDialogFragment(), IContentViewCreator {

    @FloatRange(from = 0.0, to = 1.0)
    open val contentViewHeightInScreenRatio: Float? = null
    @FloatRange(from = 0.0, to = 1.0)
    open val contentViewWidthInScreenRatio: Float? = null

    private var contentView: View? = null
    private val isDialogCreatedInvoked: AtomicBoolean = AtomicBoolean(false)

    override val layoutId: Int = 0

    private var isInvokeOnCreate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // if restart, because configure change, force dismiss
        if (!isInvokeOnCreate) {
            isInvokeOnCreate = true
        } else {
            dismissSafe()
        }
    }

    private var isInvokeOnCreateDialog = false

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!isInvokeOnCreateDialog) {
            isInvokeOnCreateDialog = true
        } else {
            return dialog ?: super.onCreateDialog(savedInstanceState)
        }
        val activity = activity ?: return super.onCreateDialog(null)

        val contentView = tryCreateNewContentView(context = activity, parentView = activity.window.decorView as? ViewGroup)
        val dialog = if (contentView != null) {
            (contentView.parent as? ViewGroup)?.removeAllViews()
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
            createDialog(contentView)
        } else {
            tUiUtilsLog.w(this::class.java.name, "No content view.")
            super.onCreateDialog(null)
        }
        firstLaunchInitData()
        return dialog
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater =  super.onGetLayoutInflater(savedInstanceState)
        if (dialog != null && isDialogCreatedInvoked.compareAndSet(false , true)) {
            contentView?.let {
                bindContentView(it)
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
    abstract fun firstLaunchInitData()

    /**
     * Do UI update.
     */
    abstract fun bindContentView(view: View)

    open fun createDialog(contentView: View): Dialog {
        return requireActivity().createDefaultDialog(contentView = contentView)
    }

    fun dismissSafe(callback: ((e: Throwable?) -> Unit)? = null) {
        val r = Runnable {
            try {
                dismissAllowingStateLoss()
                callback?.invoke(null)
            } catch (e: Throwable) {
                e.printStackTrace()
                callback?.invoke(e)
            }
        }
        if (Looper.myLooper() === Looper.getMainLooper()) {
            r.run()
        } else {
            Handler(Looper.getMainLooper()).post {
                r.run()
            }
        }
    }

    fun showSafe(fragmentManager: FragmentManager, tag: String, callback: ((e: Throwable?) -> Unit)? = null) {
        val r = Runnable {
            try {
                showNow(fragmentManager, tag)
                callback?.invoke(null)
            } catch (e: Throwable) {
                e.printStackTrace()
                callback?.invoke(e)
            }
        }
        if (Looper.myLooper() === Looper.getMainLooper()) {
            r.run()
        } else {
            Handler(Looper.getMainLooper()).post {
                r.run()
            }
        }
    }

}