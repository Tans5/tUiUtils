package com.tans.tuiutils.dialog

import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

interface CancelableResult<Result : Any> {

    fun onResult(t: Result): Boolean

    fun onCancel(): Boolean

}

class CancelableCallbackController<Result : Any>(val callback: DialogCancelableResultCallback<Result>?) : CancelableResult<Result> {
    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    override fun onResult(t: Result): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onResult(t)
            true
        } else {
            false
        }
    }

    override fun onCancel(): Boolean {
        return if (hasInvokeCallback.compareAndSet(false, true)) {
            callback?.onCancel()
            true
        } else {
            false
        }
    }
}

abstract class BaseStateCancelableResultDialogFragment<State : Any, Result : Any>(defaultState: State, callback: DialogCancelableResultCallback<Result>?) : BaseCoroutineStateDialogFragment<State>(defaultState), CancelableResult<Result> {
    private val controller = CancelableCallbackController(callback)

    override fun onResult(t: Result): Boolean {
        return if (controller.onResult(t)) {
            dismiss()
            true
        } else {
            false
        }
    }

    override fun onCancel(): Boolean {
        return if (controller.onCancel()) {
            dismiss()
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

abstract class BaseCancelableResultDialogFragment<Result : Any>(callback: DialogCancelableResultCallback<Result>?) : BaseDialogFragment(), CancelableResult<Result> {
    private val controller = CancelableCallbackController(callback)

    override fun onResult(t: Result): Boolean {
        return if (controller.onResult(t)) {
            dismiss()
            true
        } else {
            false
        }
    }

    override fun onCancel(): Boolean {
        return if (controller.onCancel()) {
            dismiss()
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

class ContinuationDialogCancelableResultCallback<T : Any> : DialogCancelableResultCallback<T> {

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

abstract class BaseContinuationStateCancelableResultDialogFragment<State : Any, Result: Any>(s: State, private val callback: ContinuationDialogCancelableResultCallback<Result> = ContinuationDialogCancelableResultCallback()) : BaseStateCancelableResultDialogFragment<State, Result>(defaultState = s, callback = callback) {

    fun attachContinuation(cont: CancellableContinuation<Result?>) {
        callback.attachContinuation(cont)
    }
}

abstract class BaseContinuationCancelableResultDialogFragment<Result: Any>(private val callback: ContinuationDialogCancelableResultCallback<Result> = ContinuationDialogCancelableResultCallback()) : BaseCancelableResultDialogFragment<Result>(callback = callback) {

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

suspend fun <State: Any, Result: Any> FragmentManager.showContinuationStateCancelableResultDialogFragment(dialog: BaseContinuationStateCancelableResultDialogFragment<State, Result>): Result? {
    return suspendCancellableCoroutine { cont ->
        dialog.attachContinuation(cont)
        if (!coroutineShowSafe(dialog, "${dialog::class.java.name}#${System.currentTimeMillis()}", cont)) {
            if (cont.isActive) {
                cont.resume(null)
            }
        }
    }
}

suspend fun <Result: Any> FragmentManager.showContinuationCancelableResultDialogFragment(dialog: BaseContinuationCancelableResultDialogFragment<Result>): Result? {
    return suspendCancellableCoroutine { cont ->
        dialog.attachContinuation(cont)
        if (!coroutineShowSafe(dialog, "${dialog::class.java.name}#${System.currentTimeMillis()}", cont)) {
            if (cont.isActive) {
                cont.resume(null)
            }
        }
    }
}