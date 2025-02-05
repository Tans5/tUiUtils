package com.tans.tuiutils.activity

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.ConcurrentHashMap

internal class FieldsViewModel : ViewModel() {

    private val savedFields: ConcurrentHashMap<String, Any> by lazy {
        ConcurrentHashMap()
    }

    private var clearObserver: ViewModelClearObserver? = null

    @Volatile private var isCleared: Boolean = false

    @MainThread
    fun setViewModelClearObserver(o: ViewModelClearObserver?) {
        clearObserver = o
    }

    fun containField(key: String): Boolean = savedFields.containsKey(key)

    fun getField(key: String): Any? = savedFields[key]

    fun saveField(key: String, field: Any): Boolean {
        if (isCleared() || containField(key)) {
            return false
        }
        savedFields[key] = field
        tUiUtilsLog.d(TAG, "Save new field: $key")
        return true
    }

    fun isCleared(): Boolean = isCleared

    override fun onCleared() {
        super.onCleared()
        clearObserver?.onViewModelCleared()
        clearObserver = null
        savedFields.clear()
        isCleared = true
    }

    companion object {

        private const val TAG = "BaseActivityViewModel"
        interface ViewModelClearObserver {
            fun onViewModelCleared()
        }
    }

}