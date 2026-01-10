package com.tans.tuiutils.demo.myfragment

import android.os.Bundle
import android.view.View
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment

class MyFragmentB : BaseCoroutineStateFragment<Unit>(Unit) {

    override val layoutId: Int = R.layout.fragment_my_fragment_b

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this@MyFragmentB::class.java.simpleName} onCreate(): $savedInstanceState")
    }

    override fun firstLaunchInitData(savedInstanceState: Bundle?) {
        println("${this@MyFragmentB::class.java.simpleName}: firstLaunchInitDataCoroutine()")
    }

    override fun bindContentView(
        contentView: View,
        useLastContentView: Boolean
    ) {
        println("${this@MyFragmentB::class.java.simpleName}: bindContentViewCoroutine()")
    }

    override fun onResume() {
        super.onResume()
        println("${this::class.java.simpleName}: onResume()")
    }

    override fun onPause() {
        super.onPause()
        println("${this::class.java.simpleName}: onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("${this::class.java.simpleName}: onDestroy()")
    }
}