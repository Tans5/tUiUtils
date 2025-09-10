package com.tans.tuiutils.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.tans.tuiutils.tUiUtilsLog

/**
 * Support bind field to [androidx.lifecycle.ViewModel] lifecycle，When Activity configure change don't be recycled.
 */
abstract class BaseActivity : AppCompatActivity(), FieldsViewModel.Companion.ViewModelClearObserver, IContentViewCreator {

    open val cleanSavedFragmentStates: Boolean
        get() = true

    override val layoutId: Int
        get() = 0

    internal val fieldsViewModel : FieldsViewModel by lazy {
        ViewModelProvider(this).get<FieldsViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (cleanSavedFragmentStates && savedInstanceState != null) {
            val components = savedInstanceState.getBundle(SAVED_COMPONENTS_KEY)
            if (components != null && components.containsKey(FRAGMENT_SAVED_STATE_KEY)) {
                components.remove(FRAGMENT_SAVED_STATE_KEY)
                tUiUtilsLog.w(TAG, "Remove fragment's states: onCreate()")
            }
        }
        val isFirstLaunch = lastNonConfigurationInstance == null
        super.onCreate(savedInstanceState)
        fieldsViewModel.setViewModelClearObserver(this)
        if (isFirstLaunch) {
            firstLaunchInitData()
        }
        val contentView = tryCreateNewContentView(context = this, parentView = null)
        if (contentView != null) {
            setContentView(contentView)
            bindContentView(contentView)
        } else {
            tUiUtilsLog.w(TAG, "No content view.")
        }
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
        return ViewModelFieldLazy(
            key = key,
            ownerViewModelGetter = { fieldsViewModel },
            initializer = initializer
        )
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val components = outState.getBundle(SAVED_COMPONENTS_KEY)
        if (components != null && components.containsKey(FRAGMENT_SAVED_STATE_KEY)) {
            components.remove(FRAGMENT_SAVED_STATE_KEY)
            tUiUtilsLog.w(TAG, "Remove fragment's states: onSaveInstanceState()")
        }
    }

    override fun onViewModelCleared() {
        tUiUtilsLog.d(TAG, "ViewModel cleared")
    }

    override fun onDestroy() {
        super.onDestroy()
        tUiUtilsLog.d(TAG, "Destroyed")
        fieldsViewModel.setViewModelClearObserver(null)
    }

}

private const val FRAGMENT_SAVED_STATE_KEY = "android:support:fragments"

private const val SAVED_COMPONENTS_KEY = "androidx.lifecycle.BundlableSavedStateRegistry.key"

private const val TAG = "BaseActivity"