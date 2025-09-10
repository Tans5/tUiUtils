package com.tans.tuiutils.activity

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.ConcurrentHashMap

internal class FieldsViewModel : ViewModel() {

    private val savedFields: ConcurrentHashMap<String, Any> by lazy {
        ConcurrentHashMap()
    }

    private val clearObservers: ArrayDeque<ViewModelClearObserver> = ArrayDeque()

    @Volatile private var isCleared: Boolean = false

    @MainThread
    fun addViewModelClearObserver(o: ViewModelClearObserver) {
        if (isCleared) {
            o.onViewModelCleared()
        } else {
            clearObservers.add(o)
        }
    }

    @MainThread
    fun removeViewModelClearObserver(o: ViewModelClearObserver) {
        clearObservers.remove(o)
    }

    fun containField(key: String): Boolean = savedFields.containsKey(key)

    fun getField(key: String): Any? = savedFields[key]

    fun saveField(key: String, field: Any): Boolean {
        if (containField(key)) {
            return false
        }
        savedFields[key] = field
        tUiUtilsLog.d(TAG, "Save new field: $key")
        return true
    }

    fun isCleared(): Boolean = isCleared

    override fun onCleared() {
        super.onCleared()
        tUiUtilsLog.d(TAG, "View model cleared")
        for (o in clearObservers) {
            o.onViewModelCleared()
        }
        clearObservers.clear()
        isCleared = true
    }

    companion object {

        private const val TAG = "BaseActivityViewModel"
        interface ViewModelClearObserver {
            fun onViewModelCleared()
        }
    }

}