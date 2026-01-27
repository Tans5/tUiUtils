package com.tans.tuiutils.state

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

open class CoroutineStateViewModel<State: Any>(defaultState: State, private val valueStorage: ValueStorage = SimpleValueStorage()) : ViewModel(), CoroutineState<State> by CoroutineState(defaultState), ValueStorage by valueStorage {

    private val actionChannel =
        Channel<Action<State>>(capacity = Channel.UNLIMITED, onUndeliveredElement = {
            it.onDropped()
            Log.w(tag, "Undelivered event: $it")
        })

    private val executedActionsCount = AtomicInteger(0)

    @Volatile
    private var isCleared: Boolean = false

    private val viewModelClearListeners: MutableList<ViewModelClearListener> = mutableListOf()

    open val tag = "CoroutineStateViewModel"

    val viewModelScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (action in actionChannel) {
                Log.d(tag, "Start execute action: $action")
                try {
                    action.onPreExecute()
                    updateSuspend { action.onExecute(it) }
                    action.onPostExecute()
                } catch (e: Throwable) {
                    Log.e(tag, "Execute action fail: ${e.message}", e)
                    action.onDropped()
                }
                Log.d(tag, "Execute action finished: $action")
                executedActionsCount.addAndGet(1)
            }
            Log.d(tag, "All actions finished.")
        }
    }

    fun addViewModelClearListener(l: ViewModelClearListener) {
        if (isCleared) {
            l.onViewModelCleared()
        } else {
            this.viewModelClearListeners.add(l)
        }
    }

    fun removeViewModelClearListener(l: ViewModelClearListener) {
        viewModelClearListeners.remove(l)
    }

    fun enqueueAction(action: Action<State>) {
        if (!isCleared) {
            actionChannel.trySend(action)
                .onSuccess {
                    Log.d(tag, "Enqueue action success: $action")
                }
                .onClosed {
                    Log.e(tag, "Enqueue action fail: $action, because channel closed: ${it?.message}", it)
                    action.onDropped()
                }
                .onFailure {
                    Log.e(tag, "Enqueue action fail: $action, because channel error: ${it?.message}", it)
                    action.onDropped()
                }
        } else {
            Log.e(tag, "Enqueue action fail: $action, because cleared")
            action.onDropped()
        }
    }

    fun enqueueAction(
        onExecute: (State) -> State,
        onPreExecute: suspend () -> Unit = {},
        onDropped: () -> Unit = {},
        onPostExecute: suspend () -> Unit
    ) {
        enqueueAction(object : Action<State>() {

            override suspend fun onPreExecute() {
                onPreExecute()
            }

            override suspend fun onExecute(oldState: State): State {
                return onExecute(oldState)
            }

            override suspend fun onPostExecute() {
                onPostExecute()
            }

            override fun onDropped() {
                onDropped()
            }

        })
    }

    fun executeActionsCount(): Int = executedActionsCount.get()

    override fun onCleared() {
        Log.d(tag, "ViewModel cleared")
        isCleared = true
        super.onCleared()
        actionChannel.close()
        viewModelScope.cancel("ViewModel cleared")
        for (l in viewModelClearListeners) {
            l.onViewModelCleared()
        }
        viewModelClearListeners.clear()
        valueStorage.clean()
    }
}


abstract class Action<State : Any> {

    open suspend fun onPreExecute() {

    }

    abstract suspend fun onExecute(oldState: State): State

    open suspend fun onPostExecute() {

    }

    open fun onDropped() {

    }
}

fun interface ViewModelClearListener {
    fun onViewModelCleared()
}