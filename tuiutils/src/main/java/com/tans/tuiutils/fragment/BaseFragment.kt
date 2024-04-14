package com.tans.tuiutils.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tans.tuiutils.tUiUtilsLog

/**
 * This fragment will be store in Activity's ViewModelStore, when configure change cause restart won't create new fragment.
 */
@Suppress("DEPRECATION")
abstract class BaseFragment : Fragment() {

    @get:LayoutRes
    abstract val layoutId: Int

    open val configureChangeCreateNewContentView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        firstLaunchInitData()
        if (savedInstanceState != null) {
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
            bindContentView(newContentView, false)
            newContentView
        } else {
            val contentView = if (savedInstanceState != null && configureChangeCreateNewContentView) {
                // Configure change and need create new view.
                tUiUtilsLog.d(BASE_FRAGMENT_TAG, "Config changed, create new ContentView")
                val newContentView = inflater.inflate(layoutId, container, false)
                bindContentView(newContentView, false)
                newContentView
            } else {
                // Use last content view.
                bindContentView(lastContentView, true)
                lastContentView
            }
            this.lastContentView = contentView
            contentView
        }
    }

    abstract fun firstLaunchInitData()

    abstract fun bindContentView(contentView: View, useLastContentView: Boolean)

    override fun onDestroy() {
        super.onDestroy()
        if (configureChangeCreateNewContentView) {
            lastContentView = null
        }
    }

}
private const val BASE_FRAGMENT_TAG = "BaseFragment"