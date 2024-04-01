package com.tans.tuiutils.activity

import android.view.View
import com.tans.tuiutils.state.CoroutineState
import com.tans.tuiutils.tUiUtilsLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseCoroutineStateActivity<State : Any>(defaultState: State) :
    BaseActivity(), CoroutineState<State> {

    override val stateFlow: MutableStateFlow<State> by lazyViewModelField("stateFlow") {
        MutableStateFlow(defaultState)
    }

    private val uiCoroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        object : CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                onUICoroutineScopeException(context, exception)
                tUiUtilsLog.e(COROUTINE_TAG, "UI CoroutineScope error: ${exception.message}", exception)
            }
        }
    }

    protected val uiCoroutineScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.Main.immediate + uiCoroutineExceptionHandler)
    }

    private val dataCoroutineExceptionHandler: CoroutineExceptionHandler by lazyViewModelField("dataCoroutineExceptionHandler") {
        object : CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                onDataCoroutineScopeException(context, exception)
                tUiUtilsLog.e(COROUTINE_TAG, "Data CoroutineScope error: ${exception.message}", exception)
            }
        }
    }

    protected val dataCoroutineScope: CoroutineScope by lazyViewModelField("dataCoroutineScope") {
        CoroutineScope(Dispatchers.IO + dataCoroutineExceptionHandler)
    }

    abstract fun CoroutineScope.firstLaunchInitDataCoroutine()

    final override fun firstLaunchInitData() {
        dataCoroutineScope.firstLaunchInitDataCoroutine()
    }

    abstract fun CoroutineScope.bindContentViewCoroutine(contentView: View)

    final override fun bindContentView(contentView: View) {
        uiCoroutineScope.bindContentViewCoroutine(contentView)
    }

    override fun onDestroy() {
        super.onDestroy()
        uiCoroutineScope.cancel("Activity destroyed.")
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        dataCoroutineScope.cancel("ViewModel cleared.")
    }

    open fun onUICoroutineScopeException(context: CoroutineContext, exception: Throwable) {

    }

    open fun onDataCoroutineScopeException(context: CoroutineContext, exception: Throwable) {

    }

}

private const val COROUTINE_TAG = "BaseCoroutineStateActivity"