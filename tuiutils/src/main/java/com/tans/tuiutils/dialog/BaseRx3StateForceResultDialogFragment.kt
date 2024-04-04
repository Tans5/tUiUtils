package com.tans.tuiutils.dialog

import android.app.Dialog
import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("MemberVisibilityCanBePrivate", "SameParameterValue")
abstract class BaseRx3StateForceResultDialogFragment<State : Any, Result : Any>(
    val defaultState: State,
    private val callback: DialogForceResultCallback<Result>?
) : BaseRx3StateDialogFragment<State>(defaultState) {

    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    protected fun onResult(t: Result): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onResult(t)
            dismissSafe()
            true
        } else {
            false
        }
    }

    protected fun onError(e: String): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onError(e)
            true
        } else {
            false
        }
    }

    override fun createDialog(contentView: View): Dialog {
        return requireActivity().createDefaultDialog(
            contentView = contentView,
            isCancelable = false
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        onError("FragmentDialog exit unexpectedly.")
    }
}