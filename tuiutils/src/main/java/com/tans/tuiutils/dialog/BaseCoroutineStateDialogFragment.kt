package com.tans.tuiutils.dialog

import com.tans.tuiutils.state.Action
import com.tans.tuiutils.state.CoroutineStateLifecycleOwner
import com.tans.tuiutils.state.CoroutineStateViewModel

abstract class BaseCoroutineStateDialogFragment<State : Any>(defaultState: State) :
    BaseDialogFragment(), CoroutineStateLifecycleOwner<State> {

    private val coroutineStateLifecycleOwnerDelegate: CoroutineStateLifecycleOwner<State> =
        CoroutineStateLifecycleOwner(
            defaultState = defaultState,
            lifecycleOwner = this,
            viewModelStoreOwner = this,
            viewModelClearListener = this
        )

    override val viewModel: CoroutineStateViewModel<State>
        get() = coroutineStateLifecycleOwnerDelegate.viewModel

    override val viewModelFieldKeyPrefix: String
        get() = coroutineStateLifecycleOwnerDelegate.viewModelFieldKeyPrefix

    override fun <T : Any> lazyViewModelField(
        key: String,
        initializer: () -> T
    ): Lazy<T> = coroutineStateLifecycleOwnerDelegate.lazyViewModelField(key, initializer)

    override fun enqueueAction(action: Action<State>) =
        coroutineStateLifecycleOwnerDelegate.enqueueAction(action)

    override fun enqueueAction(
        onExecute: (State) -> State,
        onPreExecute: suspend () -> Unit,
        onDropped: () -> Unit,
        onPostExecute: suspend () -> Unit
    ) {
        coroutineStateLifecycleOwnerDelegate.enqueueAction(
            onExecute = onExecute,
            onPreExecute = onPreExecute,
            onDropped = onDropped,
            onPostExecute = onPostExecute
        )
    }

    override fun executeActionsCount(): Int =
        coroutineStateLifecycleOwnerDelegate.executeActionsCount()

    override fun onViewModelCleared() {
    }
}