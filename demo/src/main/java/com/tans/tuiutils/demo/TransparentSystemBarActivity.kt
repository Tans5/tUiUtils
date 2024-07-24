package com.tans.tuiutils.demo

import android.view.View
import com.tans.tuiutils.activity.BaseCoroutineStateActivity
import com.tans.tuiutils.systembar.annotation.SystemBarStyle
import kotlinx.coroutines.CoroutineScope

@SystemBarStyle
class TransparentSystemBarActivity : BaseCoroutineStateActivity<Unit>(Unit) {

    override val layoutId: Int = R.layout.activity_transparent_system_bar

    private val testArg: TestArg by lazyViewModelField("testArg") {
        TestArg("TestArg: ${this.hashCode()}")
    }

    override fun CoroutineScope.firstLaunchInitDataCoroutine() {

    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {

    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        println(testArg)
    }

    companion object {
        data class TestArg(val value: String)
    }
}