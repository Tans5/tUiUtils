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

    override val layoutId: Int
        get() = 0

    internal val fieldsViewModel : FieldsViewModel by lazy {
        ViewModelProvider(this).get<FieldsViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
            tUiUtilsLog.w(this::class.java.name, "No content view.")
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
            ownerActivityGetter = { this },
            initializer = initializer
        )
    }

    fun <T : Any> lazyViewModelField(initializer: () -> T): Lazy<T> {
        return ViewModelFieldLazy(
            ownerActivityGetter = { this },
            initializer = initializer
        )
    }

    override fun onViewModelCleared() {}

    override fun onDestroy() {
        super.onDestroy()
        fieldsViewModel.setViewModelClearObserver(null)
    }

}