package com.tans.tuiutils.dialog

import com.tans.tuiutils.state.CoroutineState
import com.tans.tuiutils.tUiUtilsLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

abstract class BaseCoroutineStateDialogFragment<State : Any>(defaultState: State) : BaseDialogFragment(),
    CoroutineState<State> by CoroutineState(defaultState), CoroutineScope {

    private val coroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        object : CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                onCoroutineScopeException(context, exception)
                tUiUtilsLog.e(COROUTINE_TAG, "CoroutineScope error: ${exception.message}", exception)
            }
        }
    }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Main.immediate + coroutineExceptionHandler + Job()
    }

    open fun onCoroutineScopeException(context: CoroutineContext, exception: Throwable) {
        throw exception
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel("DialogFragment destroyed.")
    }

}

private const val COROUTINE_TAG = "BaseCoroutineStateDialogFragment"