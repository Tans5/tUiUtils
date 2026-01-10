package com.tans.tuiutils.demo.myfragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.tans.tuiutils.activity.BaseCoroutineStateActivity
import com.tans.tuiutils.view.clicks
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.demo.databinding.ActivityMyFragmentBinding
import com.tans.tuiutils.systembar.annotation.ContentViewFitSystemWindow
import com.tans.tuiutils.systembar.annotation.SystemBarStyle
import kotlinx.coroutines.CoroutineScope

@SystemBarStyle
@ContentViewFitSystemWindow
class MyFragmentActivity : BaseCoroutineStateActivity<MyFragmentActivity.Companion.State>(State()) {

    override val layoutId: Int = R.layout.activity_my_fragment

    private val myFragments: Map<FragmentType, Fragment> by lazyViewModelField("fragments") {
        mapOf(FragmentType.A to MyFragmentA(), FragmentType.B to MyFragmentB())
    }

    override fun firstLaunchInitData(savedInstanceState: Bundle?) {
    }

    override fun bindContentView(contentView: View) {
        val viewBinding = ActivityMyFragmentBinding.bind(contentView)

        stateFlow().updateUI({ it.selectedFragment }) { selectedType ->
            val fm = this@MyFragmentActivity.supportFragmentManager
            val tc = fm.beginTransaction()

            for ((t, f) in myFragments) {
                val findResult = fm.findFragmentByTag(t.name)
                if (t == selectedType) {
                    if (findResult == null) {
                        tc.add(R.id.fragment_container, f, t.name)
                    }
                    tc.setMaxLifecycle(f, Lifecycle.State.RESUMED)
                        .show(f)
                } else {
                    if (findResult != null) {
                        tc.setMaxLifecycle(f, Lifecycle.State.CREATED)
                            .hide(f)
                    }
                }
            }
            tc.commitAllowingStateLoss()
        }

        viewBinding.showFragmentABt.clicks(lifecycleScope) {
            updateState { it.copy(selectedFragment = FragmentType.A) }
        }

        viewBinding.showFragmentBBt.clicks(lifecycleScope) {
            updateState { it.copy(selectedFragment = FragmentType.B) }
        }
    }

    companion object {
        enum class FragmentType {
            A, B
        }

        data class State(
            val selectedFragment: FragmentType = FragmentType.A
        )
    }
}