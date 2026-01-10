package com.tans.tuiutils.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tans.tuiutils.tUiUtilsLog


abstract class BaseActivity : AppCompatActivity(), IContentViewCreator {

    open val cleanSavedFragmentStates: Boolean
        get() = true

    override val layoutId: Int
        get() = 0

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
        if (isFirstLaunch) {
            firstLaunchInitData(savedInstanceState)
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
    abstract fun firstLaunchInitData(savedInstanceState: Bundle?)

    /**
     * Do UI update.
     */
    abstract fun bindContentView(contentView: View)



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val components = outState.getBundle(SAVED_COMPONENTS_KEY)
        if (components != null && components.containsKey(FRAGMENT_SAVED_STATE_KEY)) {
            components.remove(FRAGMENT_SAVED_STATE_KEY)
            tUiUtilsLog.w(TAG, "Remove fragment's states: onSaveInstanceState()")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tUiUtilsLog.d(TAG, "Destroyed")
    }

}

private const val FRAGMENT_SAVED_STATE_KEY = "android:support:fragments"

private const val SAVED_COMPONENTS_KEY = "androidx.lifecycle.BundlableSavedStateRegistry.key"

private const val TAG = "BaseActivity"