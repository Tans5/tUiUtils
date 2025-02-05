package com.tans.tuiutils.activity

import java.util.concurrent.atomic.AtomicLong

internal class ViewModelFieldLazy<T : Any>: Lazy<T> {

    private val key: String
    private val ownerActivityGetter: () -> BaseActivity?
    private val initializer: () -> T

    constructor(
        key: String,
        ownerActivityGetter: () -> BaseActivity?,
        initializer: () -> T
    ) {
        this.key = key
        this.ownerActivityGetter = ownerActivityGetter
        this.initializer = initializer
    }

    constructor(
        ownerActivityGetter: () -> BaseActivity?,
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
            if (ownerActivity?.application == null) {
                error("Can't init view model lazy field, because activity is not attached.")
            }
            val viewModel = ownerActivity.fieldsViewModel
            if (viewModel.isCleared()) {
                error("ViewModel was cleared.")
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
                            error("ViewModel was cleared or use wrong key.")
                        }
                    }
                }
            }
            return result!!
        }

    override fun isInitialized(): Boolean {
        val ownerActivity = ownerActivityGetter()
        return ownerActivity?.application != null && ownerActivity.fieldsViewModel.containField(key)
    }

    companion object {
        private val anonymousViewModelFieldIndex = AtomicLong(0)
    }

}