package com.tans.tuiutils.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope

abstract class BaseLazyMemberViewModelActivity : AppCompatActivity(), LifecycleEventObserver {

    @Suppress("UNCHECKED_CAST")
    private class LazyMemberViewModel : ViewModel() {

        private val lazyMembers: HashMap<String, Lazy<*>> by lazy {
            HashMap()
        }

        private var isCleared: Boolean = false

        @Synchronized
        fun <T> registerLazyMember(key: String, initializer: () -> T): Lazy<T> {
            if (isCleared) {
                error("Can't register lazy member, ViewModel was cleared")
            }
            val last = lazyMembers[key]
            return if (last != null) {
                last as Lazy<T>
            } else {
                val new = lazy(initializer)
                lazyMembers[key] = new
                new
            }
        }

        override fun onCleared() {
            super.onCleared()
            synchronized(this) {
                lazyMembers.clear()
                isCleared = true
            }
        }
    }

    private val lazyMemberViewModel : LazyMemberViewModel by lazy {
        ViewModelProvider(this).get<LazyMemberViewModel>()
    }

    @get:LayoutRes
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycle.addObserver(this)
        super.onCreate(savedInstanceState)
        val contentView = LayoutInflater.from(this).inflate(layoutId, null, false)
        setContentView(contentView)
        bindContentView(contentView)
        if (savedInstanceState == null) {
            firstLaunchInitData()
        }
        lifecycleScope
    }

    abstract fun bindContentView(contentView: View)

    abstract fun firstLaunchInitData()

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onLifeEventCreate()
            Lifecycle.Event.ON_START -> onLifeEventStart()
            Lifecycle.Event.ON_RESUME -> onLifeEventResume()
            Lifecycle.Event.ON_PAUSE -> onLifeEventPause()
            Lifecycle.Event.ON_STOP -> onLifeEventStop()
            Lifecycle.Event.ON_DESTROY -> onLifeEventDestroy()
            Lifecycle.Event.ON_ANY -> {}
        }
    }

    protected fun <T> lazyViewModel(key: String, initializer: () -> T): Lazy<T> {
        return lazyMemberViewModel.registerLazyMember(key, initializer)
    }

    protected inline fun <reified T> lazyViewModel(noinline initializer: () -> T): Lazy<T> {
        return lazyViewModel(T::class.java.name, initializer)
    }

    open fun onLifeEventCreate() {}

    open fun onLifeEventStart() {}

    open fun onLifeEventResume() {}

    open fun onLifeEventPause() {}

    open fun onLifeEventStop() {}

    open fun onLifeEventDestroy() {}


}