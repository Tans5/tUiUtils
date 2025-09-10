package com.tans.tuiutils.fragment

import android.view.View
import com.tans.tuiutils.state.CoroutineState
import com.tans.tuiutils.tUiUtilsLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseCoroutineStateFragment<State : Any>(protected val defaultState: State) : BaseFragment(),
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

    open val firstLaunchCheckDefaultState: Boolean = true

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

    protected val dataCoroutineScope: CoroutineScope by lazyViewModelField("dataCoroutineScope") {
        CoroutineScope(Dispatchers.IO + dataCoroutineExceptionHandler)
    }

    abstract fun CoroutineScope.firstLaunchInitDataCoroutine()

    final override fun firstLaunchInitData() {
        if (firstLaunchCheckDefaultState) {
            if (defaultState == currentState()) {
                dataCoroutineScope.firstLaunchInitDataCoroutine()
            }
        } else {
            dataCoroutineScope.firstLaunchInitDataCoroutine()
        }
    }

    abstract fun CoroutineScope.bindContentViewCoroutine(contentView: View)

    final override fun bindContentView(contentView: View, useLastContentView: Boolean) {
        val newUiCoroutineScope =
            CoroutineScope(Dispatchers.Main.immediate + uiCoroutineExceptionHandler)
        newUiCoroutineScope.bindContentViewCoroutine(contentView)
        uiCoroutineScope?.let {
            if (it.isActive) {
                it.cancel("ContentView recreate.")
            }
        }
        uiCoroutineScope = newUiCoroutineScope
    }

    open fun onUICoroutineScopeException(context: CoroutineContext, exception: Throwable) {
        throw exception
    }

    open fun onDataCoroutineScopeException(context: CoroutineContext, exception: Throwable) {
        throw exception
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiCoroutineScope?.cancel("Fragment content view destroyed.")
        uiCoroutineScope = null
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        dataCoroutineScope.cancel("Fragment ViewModel cleared..")
    }

}

private const val BASE_COROUTINE_STATE_FRAGMENT_TAG = "BaseCoroutineStateFragment"