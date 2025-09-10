package com.tans.tuiutils.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tans.tuiutils.activity.IContentViewCreator
import com.tans.tuiutils.activity.tryCreateNewContentView
import com.tans.tuiutils.tUiUtilsLog

/**
 * This fragment will be store in Activity's ViewModelStore, when configure change cause restart won't create new fragment.
 */
abstract class BaseFragment : Fragment(), IContentViewCreator {

    @get:LayoutRes
    override val layoutId: Int = 0

    open val configureChangeCreateNewContentView: Boolean = false

    private var isInvokeInitData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isInvokeInitData) {
            isInvokeInitData = true
            firstLaunchInitData()
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
            val newContentView = tryCreateNewContentView(requireContext(), container)
            this.lastContentView = newContentView
            if (newContentView != null) {
                bindContentView(newContentView, false)
            } else {
                tUiUtilsLog.w(BASE_FRAGMENT_TAG, "No content view.")
            }
            newContentView
        } else {
            val contentView = if (savedInstanceState != null && configureChangeCreateNewContentView) {
                // Configure change and need create new view.
                tUiUtilsLog.d(BASE_FRAGMENT_TAG, "Config changed, create new ContentView")
                val newContentView = tryCreateNewContentView(requireContext(), container)
                if (newContentView != null) {
                    bindContentView(newContentView, false)
                } else {
                    tUiUtilsLog.w(BASE_FRAGMENT_TAG, "No content view.")
                }
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