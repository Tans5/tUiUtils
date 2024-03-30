package com.tans.tuiutils.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get

abstract class BaseLazyMemberViewModelActivity : AppCompatActivity(), LazyMemberViewModel.Companion.ViewModelClearObserver {

    private val lazyMemberViewModel : LazyMemberViewModel by lazy {
        ViewModelProvider(this).get<LazyMemberViewModel>()
    }

    @get:LayoutRes
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyMemberViewModel.setViewModelClearObserver(this)
        if (savedInstanceState == null) {
            firstLaunchInitData()
        }
        val contentView = LayoutInflater.from(this).inflate(layoutId, null, false)
        setContentView(contentView)
        bindContentView(contentView)
    }

    abstract fun firstLaunchInitData()

    abstract fun bindContentView(contentView: View)

    fun <T : Any> viewModelMember(key: String, initializer: () -> T): T {
        return lazyMemberViewModel.registerLazyMember(key, initializer).value
    }

    inline fun <reified T : Any> viewModelMember(noinline initializer: () -> T): T {
        return viewModelMember(T::class.java.name, initializer)
    }

    override fun onViewModelCleared() {}

    override fun onDestroy() {
        super.onDestroy()
        lazyMemberViewModel.setViewModelClearObserver(null)
    }

}