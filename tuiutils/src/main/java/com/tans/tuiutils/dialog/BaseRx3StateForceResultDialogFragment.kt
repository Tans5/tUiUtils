package com.tans.tuiutils.dialog

import java.util.concurrent.atomic.AtomicBoolean

@Suppress("MemberVisibilityCanBePrivate", "SameParameterValue")
abstract class BaseRx3StateForceResultDialogFragment<State : Any, Result : Any>(
    val defaultState: State,
    private val callback: DialogForceResultCallback<Result>?
) : BaseRx3StateDialogFragment<State>(defaultState) {

    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    final override val isCanceledOnTouchOutside: Boolean = false
    final override val isCancelableBaseDialog: Boolean = false
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

    override fun onDestroy() {
        super.onDestroy()
        onError("FragmentDialog exit unexpectedly.")
    }
}