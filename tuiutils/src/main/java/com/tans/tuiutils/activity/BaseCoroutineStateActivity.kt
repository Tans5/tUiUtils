package com.tans.tuiutils.activity

import com.tans.tuiutils.state.Action
import com.tans.tuiutils.state.CoroutineStateLifecycleOwner
import com.tans.tuiutils.state.CoroutineStateViewModel

 abstract class BaseCoroutineStateActivity<State : Any>(defaultState: State) : BaseActivity(), CoroutineStateLifecycleOwner<State> {

     private val coroutineStateLifecycleOwnerDelegate: CoroutineStateLifecycleOwner<State> = CoroutineStateLifecycleOwner(
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

    override fun enqueueAction(action: Action<State>) = coroutineStateLifecycleOwnerDelegate.enqueueAction(action)

    override fun enqueueAction(
        action: (State) -> State,
        pre: suspend () -> Unit,
        post: suspend () -> Unit
    ) {
        coroutineStateLifecycleOwnerDelegate.enqueueAction(action, pre, post)
    }

    override fun executeActionsCount(): Int = coroutineStateLifecycleOwnerDelegate.executeActionsCount()

    override fun onViewModelCleared() {
    }
}