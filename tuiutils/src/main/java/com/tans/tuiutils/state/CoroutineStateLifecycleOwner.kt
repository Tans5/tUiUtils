@file:Suppress("UNCHECKED_CAST")

package com.tans.tuiutils.state

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.getValue
import kotlin.reflect.KClass

interface CoroutineStateLifecycleOwner<State : Any> : LifecycleOwner, CoroutineState<State>, ViewModelClearListener {

    val viewModel: CoroutineStateViewModel<State>

    val viewModelFieldKeyPrefix: String

    override val stateFlow: MutableStateFlow<State>
        get() = viewModel.stateFlow

    /**
     * Get field value from ViewModel.
     */
    fun <T : Any> lazyViewModelField(key: String, initializer: () -> T): Lazy<T>

    fun enqueueAction(action: Action<State>)

    fun enqueueAction(
        onExecute: (State) -> State,
        onPreExecute: suspend () -> Unit = {},
        onDropped: () -> Unit = {},
        onPostExecute: suspend () -> Unit
    )

    fun executeActionsCount(): Int

    fun <T, R> Flow<T>.updateUI(
        map: (suspend (s: T) -> R),
        update: suspend (s: R) -> Unit
    ) {
        this.updateUI(lifecycleScope, map, update)
    }

    fun <T> Flow<T>.updateUI(
        update: suspend (s: T) -> Unit
    ) {
        this.updateUI(lifecycleScope, update)
    }

}

fun <State : Any> CoroutineStateLifecycleOwner(
    defaultState: State,
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    viewModelClearListener: ViewModelClearListener,
    valueStorage: ValueStorage = SimpleValueStorage()
): CoroutineStateLifecycleOwner<State> {

    return object : CoroutineStateLifecycleOwner<State> {

        override val viewModel: CoroutineStateViewModel<State> by ViewModelLazy(
            viewModelClass = CoroutineStateViewModel::class as KClass<CoroutineStateViewModel<State>>,
            storeProducer = { viewModelStoreOwner.viewModelStore },
            factoryProducer = {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return CoroutineStateViewModel(defaultState, valueStorage) as T
                    }
                }
            }
        )

        override val lifecycle: Lifecycle
            get() = lifecycleOwner.lifecycle

        override val viewModelFieldKeyPrefix: String = lifecycleOwner.toString()

        init {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)
                    viewModel.addViewModelClearListener(viewModelClearListener)
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    viewModel.removeViewModelClearListener(viewModelClearListener)
                }
            })
        }


        /**
         * Get field value from ViewModel.
         */
        override fun <T : Any> lazyViewModelField(key: String, initializer: () -> T): Lazy<T> {
            return FieldLazy(
                key = "${viewModelFieldKeyPrefix}_$key",
                storageProvider = { viewModel },
                initializer = initializer
            )
        }

        override fun enqueueAction(action: Action<State>) {
            viewModel.enqueueAction(action)
        }

        override fun enqueueAction(
            onExecute: (State) -> State,
            onPreExecute: suspend () -> Unit,
            onDropped: () -> Unit,
            onPostExecute: suspend () -> Unit
        ) {
            viewModel.enqueueAction(
                onExecute = onExecute,
                onPreExecute = onPreExecute,
                onDropped = onDropped,
                onPostExecute = onPostExecute
            )
        }

        override fun executeActionsCount(): Int = viewModel.executeActionsCount()


        override fun onViewModelCleared() {
        }
    }
}
