package com.tans.tuiutils.demo.mediastore

import android.os.Bundle
import android.view.View
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import kotlinx.coroutines.CoroutineScope

class AudiosFragment : BaseCoroutineStateFragment<Unit>(Unit) {

    override val layoutId: Int = R.layout.fragment_audios

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this@AudiosFragment::class.java.simpleName} onCreate(): $savedInstanceState")
    }

    override fun CoroutineScope.firstLaunchInitDataCoroutine() {
        println("${this@AudiosFragment::class.java.simpleName} firstLaunchInitDataCoroutine()")
    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        println("${this@AudiosFragment::class.java.simpleName} bindContentViewCoroutine()")
    }

    override fun onResume() {
        super.onResume()
        println("${this@AudiosFragment::class.java.simpleName} onResume()")
    }

    override fun onPause() {
        super.onPause()
        println("${this@AudiosFragment::class.java.simpleName} onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("${this@AudiosFragment::class.java.simpleName} onDestroy()")
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        println("${this@AudiosFragment::class.java.simpleName} onViewModelCleared()")
    }
}