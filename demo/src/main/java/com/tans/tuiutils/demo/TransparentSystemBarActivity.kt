package com.tans.tuiutils.demo

import android.os.Bundle
import android.view.View
import com.tans.tuiutils.activity.BaseCoroutineStateActivity
import com.tans.tuiutils.systembar.annotation.SystemBarStyle

@SystemBarStyle
class TransparentSystemBarActivity : BaseCoroutineStateActivity<Unit>(Unit) {

    override val layoutId: Int = R.layout.activity_transparent_system_bar

    override fun firstLaunchInitData(savedInstanceState: Bundle?) {
    }

    override fun bindContentView(contentView: View) {
    }

    private val testArg: TestArg by lazyViewModelField("testArg") {
        TestArg("TestArg: ${this.hashCode()}")
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        println(testArg)
    }

    companion object {
        data class TestArg(val value: String)
    }
}