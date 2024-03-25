package com.tans.tuiutils.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


interface CoroutineState<State : Any> {

    val stateFlow: MutableStateFlow<State>

    fun currentState(): State = stateFlow.value

    suspend fun updateStateSuspend(update: suspend (oldState: State) -> State): State {
        stateFlow.update { update(it) }
        return stateFlow.value
    }

    fun updateState(update: (oldState: State) -> State): State {
        stateFlow.update { update(it) }
        return stateFlow.value
    }

    suspend fun <T : Any> renderStateSuspend(
        m: suspend (s: State) -> T,
        render: suspend (t: T) -> Unit
    ) {
        stateFlow
            .map(m)
            .distinctUntilChanged()
            .flowOn(Dispatchers.Main)
            .collect {
                render(it)
            }
    }

    fun stateFlow(): Flow<State> = stateFlow

    fun <T : Any> CoroutineScope.renderStateNewCoroutine(
        m: suspend (s: State) -> T,
        render: suspend (t: T) -> Unit
    ) {
        launch {
            renderStateSuspend(m, render)
        }
    }

}

fun <State : Any> CoroutineState(defaultState: State): CoroutineState<State> {
    return object : CoroutineState<State> {

        override val stateFlow: MutableStateFlow<State> = MutableStateFlow(defaultState)
    }
}