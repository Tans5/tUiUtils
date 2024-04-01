package com.tans.tuiutils.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get

/**
 * Support bind field to [androidx.lifecycle.ViewModel] lifecycle，When Activity configure change don't be recycled.
 */
abstract class BaseActivity : AppCompatActivity(), BaseActivityViewModel.Companion.ViewModelClearObserver {

    private val baseActivityViewModel : BaseActivityViewModel by lazy {
        ViewModelProvider(this).get<BaseActivityViewModel>()
    }

    @get:LayoutRes
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseActivityViewModel.setViewModelClearObserver(this)
        if (savedInstanceState == null) {
            firstLaunchInitData()
        }
        val contentView = LayoutInflater.from(this).inflate(layoutId, null, false)
        setContentView(contentView)
        bindContentView(contentView)
    }

    /**
     * If Activity is not restart, this method would be invoke, load some init data, do not do anything about UI update.
     */
    abstract fun firstLaunchInitData()

    /**
     * Do UI update.
     */
    abstract fun bindContentView(contentView: View)

    /**
     * Get field value from ViewModel.
     */
    fun <T : Any> lazyViewModelField(key: String, initializer: () -> T): Lazy<T> {
        return ViewModelFieldLazy(key, initializer)
    }

    override fun onViewModelCleared() {}

    private inner class ViewModelFieldLazy<T : Any>(
        private val key: String,
        private val initializer: () -> T
    ) : Lazy<T> {

        @Suppress("UNCHECKED_CAST")
        override val value: T
            get() {
                if (application == null) {
                    error("Can't init view model lazy field, because activity is not attachted.")
                }
                val vm = this@BaseActivity.baseActivityViewModel
                val result: T?
                while (true) {
                    val cacheField = vm.getField(key)
                    if (cacheField != null) {
                        try {
                            cacheField as T
                            result = cacheField
                            break
                        } catch (e: Throwable) {
                            error("Wrong field type, maybe you use same key in different fields, key=$key, error=${e.message}")
                        }
                    } else {
                        val newField = initializer()
                        if (vm.saveField(key, newField)) {
                            result = newField
                            break
                        }
                    }
                }
                return result!!
            }

        override fun isInitialized(): Boolean {
            return application != null && this@BaseActivity.baseActivityViewModel.containField(key)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        baseActivityViewModel.setViewModelClearObserver(null)
    }

}