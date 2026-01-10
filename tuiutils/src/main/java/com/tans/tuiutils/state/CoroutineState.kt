package com.tans.tuiutils.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface CoroutineState<State : Any> {

    val stateFlow: MutableStateFlow<State>

    fun state(): State = stateFlow.value

    suspend fun updateSuspend(update: suspend (oldState: State) -> State) {
        stateFlow.update { update(it) }
    }

    fun updateState(update: (oldState: State) -> State) {
        stateFlow.update { update(it) }
    }

    suspend fun updateSuspend(
        expect: State,
        update: State,
        fail: suspend () -> Unit = {},
        success: suspend () -> Unit = {}): Boolean {
        return if (stateFlow.compareAndSet(expect, update)) {
            success()
            true
        } else {
            fail()
            false
        }
    }

    fun updateState(
        expect: State,
        update: State,
        fail: () -> Unit = {},
        success: () -> Unit = {}
    ): Boolean {
        return if (stateFlow.compareAndSet(expect, update)) {
            success()
            true
        } else {
            fail()
            false
        }
    }

    suspend fun updateStateSuspend(
        vararg expects: State,
        update: State,
        fail: suspend () -> Unit = {},
        success: suspend () -> Unit = {}
    ): Boolean {
        for (e in expects) {
            if (stateFlow.compareAndSet(e, update)) {
                success()
                return true
            }
        }
        fail()
        return false
    }

    fun updateState(
        expects: Array<State>,
        update: State,
        fail: () -> Unit = {},
        success: () -> Unit = {}
    ): Boolean {
        for (e in expects) {
            if (stateFlow.compareAndSet(e, update)) {
                success()
                return true
            }
        }
        fail()
        return false
    }

    suspend fun updateStateIfSuspend(
        `if`: suspend (State) -> Boolean,
        update: State,
        fail: suspend () -> Unit = {},
        success: suspend() -> Unit = {}
    ): Boolean {
        var isFail = false
        stateFlow.update { old ->
            if (`if`(old)) {
                update
            } else {
                isFail = true
                old
            }
        }
        return if (isFail) {
            fail()
            false
        } else {
            success()
            true
        }
    }

    fun updateStateIf(
        `if`: (State) -> Boolean,
        update: State,
        fail: () -> Unit = {},
        success: () -> Unit = {}
    ): Boolean {
        var isFail = false
        stateFlow.update { old ->
            if (`if`(old)) {
                update
            } else {
                isFail = true
                old
            }
        }
        return if (isFail) {
            fail()
            false
        } else {
            success()
            true
        }
    }

    fun stateFlow(): Flow<State> = stateFlow
}

fun <State : Any> CoroutineState(defaultState: State): CoroutineState<State> {
    return object : CoroutineState<State> {

        override val stateFlow: MutableStateFlow<State> = MutableStateFlow(defaultState)
    }
}

fun <T, R> Flow<T>.updateUI(
    coroutineScope: CoroutineScope,
    map: (suspend (s: T) -> R),
    update: suspend (s: R) -> Unit
) {
    coroutineScope.launch(Dispatchers.Main.immediate) {
        this@updateUI.map(map)
            .distinctUntilChanged()
            .collect {
                update(it)
            }
    }
}

fun <T> Flow<T>.updateUI(
    coroutineScope: CoroutineScope,
    update: suspend (s: T) -> Unit
) {
    coroutineScope.launch(Dispatchers.Main.immediate) {
        this@updateUI.distinctUntilChanged()
            .collect { update(it) }
    }
}