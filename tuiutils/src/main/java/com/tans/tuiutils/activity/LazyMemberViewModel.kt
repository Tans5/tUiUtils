package com.tans.tuiutils.activity

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.tans.tuiutils.tUiUtilsLog

internal class LazyMemberViewModel : ViewModel() {

    private val lazyMembers: HashMap<String, Lazy<*>> by lazy {
        HashMap()
    }

    private var isCleared: Boolean = false

    private var clearObserver: ViewModelClearObserver? = null

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun <T> registerLazyMember(key: String, initializer: () -> T): Lazy<T> {
        if (isCleared) {
            error("Can't register lazy member, ViewModel was cleared")
        }
        val last = lazyMembers[key]
        return if (last != null) {
            tUiUtilsLog.w(TAG, "Skip register new lazy member: $key")
            last as Lazy<T>
        } else {
            tUiUtilsLog.d(TAG, "Register new lazy member: $key")
            val new = lazy(initializer)
            lazyMembers[key] = new
            new
        }
    }

    @Synchronized
    fun syncWaitingLazyMembers(waitingSyncMembers: Map<String, Lazy<*>>) {
        for ((k, v) in waitingSyncMembers) {
            if (lazyMembers.containsKey(k)) {
                tUiUtilsLog.w(TAG, "Skip register new lazy member: $k")
            } else {
                lazyMembers[k] = v
                tUiUtilsLog.d(TAG, "Register new lazy member: $k")
            }
        }
    }

    @MainThread
    fun setViewModelClearObserver(o: ViewModelClearObserver?) {
        clearObserver = o
    }

    override fun onCleared() {
        super.onCleared()
        clearObserver?.onViewModelCleared()
    }

    companion object {

        private const val TAG = "LazyMemberViewModel"
        interface ViewModelClearObserver {
            fun onViewModelCleared()
        }
    }

}