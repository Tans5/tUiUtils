package com.tans.tuiutils.dialog

import android.app.Dialog
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("MemberVisibilityCanBePrivate", "SameParameterValue")
abstract class BaseCoroutineStateForceResultDialogFragment<State : Any, Result : Any>(
    defaultState: State,
    private val callback: DialogForceResultCallback<Result>?
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

    protected fun onError(e: String): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onError(e)
            true
        } else {
            false
        }
    }

    override fun createDialog(contentView: View): Dialog {
        isCancelable = false
        return requireActivity().createDefaultDialog(contentView = contentView, isCancelable = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        onError("FragmentDialog exit unexpectedly.")
    }
}

class CoroutineDialogForceResultCallback<T : Any> : DialogForceResultCallback<T> {

    private var cont: CancellableContinuation<T>? = null

    override fun onResult(t: T) {
        val cont = this.cont
        if (cont != null && cont.isActive) {
            cont.resume(t)
        }
    }

    override fun onError(e: String) {
        val cont = this.cont
        if (cont != null && cont.isActive) {
            cont.resumeWithException(Throwable(e))
        }
    }

    fun attachContinuation(cont: CancellableContinuation<T>) {
        this.cont = cont
    }

}

abstract class BaseSimpleCoroutineResultForceDialogFragment<State : Any, Result : Any>(
    s: State,
    private val callback: CoroutineDialogForceResultCallback<Result> = CoroutineDialogForceResultCallback()
) : BaseCoroutineStateForceResultDialogFragment<State, Result>(
    defaultState = s,
    callback = callback
) {
    fun attachContinuation(cont: CancellableContinuation<Result>) {
        callback.attachContinuation(cont)
    }
}

suspend fun <State: Any, Result: Any> FragmentManager.showSimpleForceCoroutineResultDialogSuspend(dialog: BaseSimpleCoroutineResultForceDialogFragment<State, Result>): Result {
    return suspendCancellableCoroutine { cont ->
        dialog.attachContinuation(cont)
        if (!coroutineShowSafe(dialog, "${dialog::class.java.name}#${System.currentTimeMillis()}", cont)) {
            if (cont.isActive) {
                cont.resumeWithException(RuntimeException("Coroutine canceled."))
            }
        }
    }
}