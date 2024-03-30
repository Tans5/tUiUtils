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
abstract class BaseViewModelFieldActivity : AppCompatActivity(), FieldSaveViewModel.Companion.ViewModelClearObserver {

    private val fieldSaveViewModel : FieldSaveViewModel by lazy {
        ViewModelProvider(this).get<FieldSaveViewModel>()
    }

    @get:LayoutRes
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fieldSaveViewModel.setViewModelClearObserver(this)
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
    fun <T : Any> viewModelField(key: String, initializer: () -> T): T {
        return fieldSaveViewModel.registerLazyField(key, initializer).value
    }

    inline fun <reified T : Any> viewModelField(noinline initializer: () -> T): T {
        return viewModelField(T::class.java.name, initializer)
    }

    override fun onViewModelCleared() {}

    override fun onDestroy() {
        super.onDestroy()
        fieldSaveViewModel.setViewModelClearObserver(null)
    }

}