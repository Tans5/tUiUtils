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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

open class CoroutineStateViewModel<State: Any>(defaultState: State, ) : ViewModel(), CoroutineState<State> by CoroutineState(defaultState) {

    private val actionChannel =
        Channel<Action<State>>(capacity = Channel.UNLIMITED, onUndeliveredElement = {
            Log.w(tag, "Undelivered event: $it")
        })

    private val executedActionsCount = AtomicInteger(0)

    private val simpleData: MutableMap<String, Any> = ConcurrentHashMap()

    @Volatile
    private var isCleared: Boolean = false

    open val tag = "CoroutineStateViewModel"

    val viewModelScope by lazy {
        object : CoroutineScope {
            override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (action in actionChannel) {
                Log.d(tag, "Start execute action: $action")
                action.preExecute()
                updateSuspend { action.execute(it) }
                action.postExecute()
                Log.d(tag, "Execute action finished: $action")
                executedActionsCount.addAndGet(1)
            }
            Log.d(tag, "All actions finished.")
        }
    }

    fun enqueueAction(action: Action<State>) {
        if (!isCleared) {
            actionChannel.trySend(action)
                .onSuccess {
                    Log.d(tag, "Enqueue action success: $action")
                }
                .onClosed {
                    Log.e(tag, "Enqueue action fail: $action, because channel closed: ${it?.message}", it)
                }
                .onFailure {
                    Log.e(tag, "Enqueue action fail: $action, because channel error: ${it?.message}", it)
                }
        } else {
            Log.e(tag, "Enqueue action fail: $action, because cleared")
        }
    }

    fun enqueueAction(
        action: (State) -> State,
        pre: suspend () -> Unit = {},
        post: suspend () -> Unit
    ) {
        enqueueAction(object : Action<State>() {

            override suspend fun preExecute() {
                pre()
            }

            override suspend fun execute(oldState: State): State {
                return action(oldState)
            }

            override suspend fun postExecute() {
                post()
            }

        })
    }

    fun executeActionsCount(): Int = executedActionsCount.get()

    inline fun <T : Any> getOrSaveSimpleData(key: String, createNew:  () -> T): T {
        val old = getSimpleData<T>(key)
        if (old != null) {
            return old
        }
        val new = createNew()
        saveSimpleData(key, new)
        return new
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getSimpleData(key: String): T? = simpleData[key] as? T

    fun saveSimpleData(key: String, value: Any) {
        simpleData[key] = value
    }

    override fun onCleared() {
        Log.d(tag, "ViewModel cleared")
        isCleared = true
        super.onCleared()
        simpleData.clear()
        actionChannel.close()
        viewModelScope.cancel("ViewModel cleared")
    }
}


abstract class Action<State : Any> {

    open suspend fun preExecute() {

    }

    abstract suspend fun execute(oldState: State): State

    open suspend fun postExecute() {

    }
}