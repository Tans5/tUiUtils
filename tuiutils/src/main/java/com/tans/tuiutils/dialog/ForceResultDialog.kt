package com.tans.tuiutils.dialog

import android.app.Dialog
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface ForceResult<Result : Any> {

    fun onResult(t: Result): Boolean

    fun onError(e: String): Boolean

}

class ForceCallbackController<Result : Any>(val callback: DialogForceResultCallback<Result>?) : ForceResult<Result> {
    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    override fun onResult(t: Result): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onResult(t)
            true
        } else {
            false
        }
    }

    override fun onError(e: String): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onError(e)
            true
        } else {
            false
        }
    }
}

abstract class BaseStateForceResultDialogFragment<State : Any, Result : Any>(defaultState: State, callback: DialogForceResultCallback<Result>?) : BaseCoroutineStateDialogFragment<State>(defaultState), ForceResult<Result> {
    private val controller = ForceCallbackController(callback)

    override fun onResult(t: Result): Boolean {
        return if (controller.onResult(t)) {
            dismissSafe()
            true
        } else {
            false
        }
    }

    override fun onError(e: String): Boolean {
        return if (controller.onError(e)) {
            dismissSafe()
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

abstract class BaseForceResultDialogFragment<Result : Any>(callback: DialogForceResultCallback<Result>?) : BaseDialogFragment(), ForceResult<Result> {
    private val controller = ForceCallbackController(callback)

    override fun onResult(t: Result): Boolean {
        return if (controller.onResult(t)) {
            dismissSafe()
            true
        } else {
            false
        }
    }

    override fun onError(e: String): Boolean {
        return if (controller.onError(e)) {
            dismissSafe()
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

class ContinuationDialogForceResultCallback<T : Any> : DialogForceResultCallback<T> {

    private var cont: CancellableContinuation<T>? = null

    override fun onError(e: String) {
        val cont = cont
        if (cont != null && cont.isActive) {
            cont.resumeWithException(Throwable(e))
        }
    }

    override fun onResult(t: T) {
        val cont = cont
        if (cont != null && cont.isActive) {
            cont.resume(t)
        }
    }

    fun attachContinuation(cont: CancellableContinuation<T>) {
        this.cont = cont
    }

}

abstract class BaseContinuationStateForceResultDialogFragment<State : Any, Result: Any>(s: State, private val callback: ContinuationDialogForceResultCallback<Result> = ContinuationDialogForceResultCallback()) : BaseStateForceResultDialogFragment<State, Result>(defaultState = s, callback = callback) {

    fun attachContinuation(cont: CancellableContinuation<Result>) {
        callback.attachContinuation(cont)
    }
}

abstract class BaseContinuationForceResultDialogFragment<Result: Any>(private val callback: ContinuationDialogForceResultCallback<Result> = ContinuationDialogForceResultCallback()) : BaseForceResultDialogFragment<Result>(callback = callback) {

    fun attachContinuation(cont: CancellableContinuation<Result>) {
        callback.attachContinuation(cont)
    }
}

suspend fun <State: Any, Result: Any> FragmentManager.showContinuationStateForceResultDialogFragment(dialog: BaseContinuationStateForceResultDialogFragment<State, Result>): Result {
    return suspendCancellableCoroutine { cont ->
        dialog.attachContinuation(cont)
        if (!coroutineShowSafe(dialog, "${dialog::class.java.name}#${System.currentTimeMillis()}", cont)) {
            if (cont.isActive) {
                cont.resumeWithException(RuntimeException("Coroutine canceled."))
            }
        }
    }
}

suspend fun <Result: Any> FragmentManager.showContinuationForceResultDialogFragment(dialog: BaseContinuationForceResultDialogFragment<Result>): Result {
    return suspendCancellableCoroutine { cont ->
        dialog.attachContinuation(cont)
        if (!coroutineShowSafe(dialog, "${dialog::class.java.name}#${System.currentTimeMillis()}", cont)) {
            if (cont.isActive) {
                cont.resumeWithException(RuntimeException("Coroutine canceled."))
            }
        }
    }
}