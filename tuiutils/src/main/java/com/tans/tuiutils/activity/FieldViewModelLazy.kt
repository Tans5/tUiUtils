package com.tans.tuiutils.activity

import com.tans.tuiutils.tUiUtilsLog

internal class ViewModelFieldLazy<T : Any>: Lazy<T> {

    private val key: String
    private val ownerViewModelGetter: () -> FieldsViewModel
    private val initializer: () -> T

    constructor(
        key: String,
        ownerViewModelGetter: () -> FieldsViewModel,
        initializer: () -> T
    ) {
        this.key = key
        this.ownerViewModelGetter = ownerViewModelGetter
        this.initializer = initializer
    }

    @Suppress("UNCHECKED_CAST")
    override val value: T
        get() {
            val viewModel = ownerViewModelGetter()
            if (viewModel.isCleared()) {
                tUiUtilsLog.e(TAG, "ViewModel was cleared.")
            }
            val firstCheckValue = viewModel.getField(key)
            var result: T? = null
            // First check.
            if (firstCheckValue != null) {
                try {
                    result = firstCheckValue as T
                } catch (e: Throwable) {
                    error("Wrong field type, maybe you use same key in different fields, key=$key, error=${e.message}")
                }
            } else {
                synchronized(this) {
                    val secondCheckValue = viewModel.getField(key)
                    // Second check.
                    result = if (secondCheckValue != null) {
                        try {
                            secondCheckValue as T
                        } catch (e: Throwable) {
                            error("Wrong field type, maybe you use same key in different fields, key=$key, error=${e.message}")
                        }
                    } else {
                        val newValue = initializer()
                        if (viewModel.saveField(key, newValue)) {
                            newValue
                        } else {
                            error("Use wrong key: $key")
                        }
                    }
                }
            }
            return result!!
        }

    override fun isInitialized(): Boolean {
        return ownerViewModelGetter().containField(key)
    }

    companion object {
        private const val TAG = "ViewModelFieldLazy"
    }

}