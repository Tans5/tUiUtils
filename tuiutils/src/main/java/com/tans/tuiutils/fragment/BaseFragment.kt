package com.tans.tuiutils.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.tans.tuiutils.tUiUtilsLog

/**
 * This fragment will be store in Activity's ViewModelStore, when configure change cause restart won't create new fragment.
 */
@Suppress("DEPRECATION")
abstract class BaseFragment : Fragment(), BaseFragmentViewModel.Companion.ViewModelClearObserver {

    @get:LayoutRes
    abstract val layoutId: Int

    private val baseFragmentViewModel: BaseFragmentViewModel by lazy {
        ViewModelProvider(this).get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseFragmentViewModel.setViewModelClearObserver(this)
        retainInstance = true
        if (savedInstanceState == null) {
            if (!baseFragmentViewModel.hasInvokeFirstLaunchInitData) {
                firstLaunchInitData()
                baseFragmentViewModel.hasInvokeFirstLaunchInitData = true
            }
        } else {
            val needRemoveFragments = parentFragmentManager.fragments.filter { it.tag == tag }
            if (needRemoveFragments.isNotEmpty()) {
                parentFragmentManager.beginTransaction().apply {
                    for (f in needRemoveFragments) {
                        remove(f)
                    }
                    commitAllowingStateLoss()
                }
                tUiUtilsLog.d(BASE_FRAGMENT_TAG, "Remove restore fragments: $needRemoveFragments")
            }
        }
    }

    private var lastContentView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val lastContentView = lastContentView
        return if (lastContentView == null) {
            tUiUtilsLog.d(BASE_FRAGMENT_TAG, "LastContentView is null, create new ContentView")
            val newContentView = inflater.inflate(layoutId, container, false)
            this.lastContentView = newContentView
            bindContentView(newContentView)
            newContentView
        } else {
            if (savedInstanceState != null) {
                tUiUtilsLog.d(BASE_FRAGMENT_TAG, "Config changed, create new ContentView")
                val newContentView = inflater.inflate(layoutId, container, false)
                this.lastContentView = newContentView
                bindContentView(newContentView)
                newContentView
            } else {
                lastContentView
            }
        }
    }

    abstract fun firstLaunchInitData()

    abstract fun bindContentView(contentView: View)

    override fun onDestroy() {
        super.onDestroy()
        lastContentView = null
    }

    fun <T : Any> lazyViewModelField(key: String, initializer: () -> T): Lazy<T> {
        return ViewModelFieldLazy(key, initializer)
    }

    private inner class ViewModelFieldLazy<T : Any>(
        private val key: String,
        private val initializer: () -> T
    ) : Lazy<T> {

        @Suppress("UNCHECKED_CAST")
        override val value: T
            get() {
                if (activity == null) {
                    error("Can't init view model lazy field, because fragment is not attached.")
                }
                val vm = this@BaseFragment.baseFragmentViewModel
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
            return activity != null && this@BaseFragment.baseFragmentViewModel.containField(key)
        }

    }

}
private const val BASE_FRAGMENT_TAG = "BaseFragment"