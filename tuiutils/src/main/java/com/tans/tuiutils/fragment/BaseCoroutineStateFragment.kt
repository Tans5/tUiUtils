package com.tans.tuiutils.fragment

import android.view.View
import com.tans.tuiutils.state.CoroutineState
import com.tans.tuiutils.tUiUtilsLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseCoroutineStateFragment<State : Any>(defaultState: State) : BaseFragment(),
    CoroutineState<State> by CoroutineState(defaultState) {

    private val uiCoroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        object : CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                onUICoroutineScopeException(context, exception)
                tUiUtilsLog.e(BASE_COROUTINE_STATE_FRAGMENT_TAG, "UI CoroutineScope error: ${exception.message}", exception)
            }
        }
    }

    protected var uiCoroutineScope: CoroutineScope? = null
        private set

    private val dataCoroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        object : CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                onDataCoroutineScopeException(context, exception)
                tUiUtilsLog.e(BASE_COROUTINE_STATE_FRAGMENT_TAG, "Data CoroutineScope error: ${exception.message}", exception)
            }
        }
    }

    protected val dataCoroutineScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.IO + dataCoroutineExceptionHandler)
    }

    abstract fun CoroutineScope.firstLaunchInitDataCoroutine()

    final override fun firstLaunchInitData() {
        dataCoroutineScope.firstLaunchInitDataCoroutine()
    }

    abstract fun CoroutineScope.bindContentViewCoroutine(contentView: View)

    final override fun bindContentView(contentView: View) {
        val newUiCoroutineScope =
            CoroutineScope(Dispatchers.Main.immediate + uiCoroutineExceptionHandler)
        uiCoroutineScope?.cancel("ContentView recreate.")
        uiCoroutineScope = newUiCoroutineScope
        newUiCoroutineScope.bindContentViewCoroutine(contentView)
    }

    open fun onUICoroutineScopeException(context: CoroutineContext, exception: Throwable) {

    }

    open fun onDataCoroutineScopeException(context: CoroutineContext, exception: Throwable) {

    }

    override fun onDestroy() {
        super.onDestroy()
        uiCoroutineScope?.cancel("Fragment destroyed.")
        dataCoroutineScope.cancel("Fragment destroyed.")
    }

}

private const val BASE_COROUTINE_STATE_FRAGMENT_TAG = "BaseCoroutineStateFragment"