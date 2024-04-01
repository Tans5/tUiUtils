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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        if (savedInstanceState == null) {
            firstLaunchInitData()
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

}
private const val BASE_FRAGMENT_TAG = "BaseFragment"