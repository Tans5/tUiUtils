package com.tans.tuiutils.dialog

import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseCoroutineStateCancelableResultDialogFragment<State : Any, Result : Any>(
    defaultState: State,
    private val callback: DialogCancelableResultCallback<Result>?
) : BaseCoroutineStateDialogFragment<State>(defaultState) {

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

class CoroutineDialogCancelableResultCallback<T : Any> : DialogCancelableResultCallback<T> {

    private var cont: CancellableContinuation<T?>? = null

    override fun onCancel() {
        val cont = cont
        if (cont != null && cont.isActive) {
            cont.resume(null)
        }
    }

    override fun onResult(t: T) {
        val cont = cont
        if (cont != null && cont.isActive) {
            cont.resume(t)
        }
    }

    fun attachContinuation(cont: CancellableContinuation<T?>) {
        this.cont = cont
    }

}

abstract class BaseSimpleCoroutineResultCancelableDialogFragment<State : Any, Result: Any>(
    s: State,
    private val callback: CoroutineDialogCancelableResultCallback<Result> = CoroutineDialogCancelableResultCallback()
) : BaseCoroutineStateCancelableResultDialogFragment<State, Result>(
    defaultState = s,
    callback = callback
) {

    fun attachContinuation(cont: CancellableContinuation<Result?>) {
        callback.attachContinuation(cont)
    }
}

internal fun FragmentManager.coroutineShowSafe(dialog: BaseDialogFragment, tag: String, cont: CancellableContinuation<*>): Boolean {
    return if (!isDestroyed) {
        dialog.showSafe(this, tag)
        val wd = WeakReference(dialog)
        cont.invokeOnCancellation {
            if (!isDestroyed) {
                wd.get()?.dismissAllowingStateLoss()
            }
        }
        true
    } else {
        false
    }
}

suspend fun <State: Any, Result: Any> FragmentManager.showSimpleCancelableCoroutineResultDialogSuspend(dialog: BaseSimpleCoroutineResultCancelableDialogFragment<State, Result>): Result? {
    return suspendCancellableCoroutine { cont ->
        dialog.attachContinuation(cont)
        if (!coroutineShowSafe(dialog, "${dialog::class.java.name}#${System.currentTimeMillis()}", cont)) {
            if (cont.isActive) {
                cont.resume(null)
            }
        }
    }
}