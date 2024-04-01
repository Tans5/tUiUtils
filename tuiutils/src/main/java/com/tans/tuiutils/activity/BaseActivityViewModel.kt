package com.tans.tuiutils.activity

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.ConcurrentHashMap

internal class BaseActivityViewModel : ViewModel() {


    private val savedFields: ConcurrentHashMap<String, Any> by lazy {
        ConcurrentHashMap()
    }

    private var clearObserver: ViewModelClearObserver? = null


    @MainThread
    fun setViewModelClearObserver(o: ViewModelClearObserver?) {
        clearObserver = o
    }

    fun containField(key: String): Boolean = savedFields.containsKey(key)

    fun getField(key: String): Any? = savedFields[key]

    fun saveField(key: String, field: Any): Boolean {
        synchronized(savedFields) {
            val lastValue = savedFields[key]
            if (lastValue != null) {
                tUiUtilsLog.w(TAG, "Skip save new field: $key")
                return false
            } else {
                savedFields[key] = field
                tUiUtilsLog.d(TAG, "Save new field: $key")
                return true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearObserver?.onViewModelCleared()
    }

    companion object {

        private const val TAG = "BaseActivityViewModel"
        interface ViewModelClearObserver {
            fun onViewModelCleared()
        }
    }

}