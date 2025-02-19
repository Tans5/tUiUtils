package com.tans.tuiutils.activity

import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.atomic.AtomicLong

internal class ViewModelFieldLazy<T : Any>: Lazy<T> {

    private val key: String
    private val ownerActivityGetter: () -> BaseActivity
    private val initializer: () -> T

    constructor(
        key: String,
        ownerActivityGetter: () -> BaseActivity,
        initializer: () -> T
    ) {
        this.key = key
        this.ownerActivityGetter = ownerActivityGetter
        this.initializer = initializer
    }

    constructor(
        ownerActivityGetter: () -> BaseActivity,
        initializer: () -> T
    ) {
        this.key = "AnonymousViewModelField#${anonymousViewModelFieldIndex.getAndIncrement()}"
        this.ownerActivityGetter = ownerActivityGetter
        this.initializer = initializer
    }

    @Suppress("UNCHECKED_CAST")
    override val value: T
        get() {
            val ownerActivity = ownerActivityGetter()
            if (ownerActivity.application == null) {
                tUiUtilsLog.e(TAG, "Activity is not attached.")
            }
            val viewModel = ownerActivity.fieldsViewModel
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
        val ownerActivity = ownerActivityGetter()
        return ownerActivity.fieldsViewModel.containField(key)
    }

    companion object {
        private val anonymousViewModelFieldIndex = AtomicLong(0)
        private const val TAG = "ViewModelFieldLazy"
    }

}