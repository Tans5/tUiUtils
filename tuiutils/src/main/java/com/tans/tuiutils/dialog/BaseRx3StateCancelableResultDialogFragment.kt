package com.tans.tuiutils.dialog

import java.util.concurrent.atomic.AtomicBoolean

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseRx3StateCancelableResultDialogFragment<State : Any, Result : Any>(
    defaultState: State,
    private val callback: DialogCancelableResultCallback<Result>?
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

    protected fun onCancel(): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onCancel()
            dismissSafe()
            true
        } else {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancel()
    }
}