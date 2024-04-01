package com.tans.tuiutils.demo.mediastore

import android.os.Bundle
import android.view.View
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import kotlinx.coroutines.CoroutineScope

class ImagesFragment : BaseCoroutineStateFragment<Unit>(Unit) {

    override val layoutId: Int = R.layout.fragment_images

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this@ImagesFragment::class.java.simpleName} onCreate(): $savedInstanceState")
    }

    override fun CoroutineScope.firstLaunchInitDataCoroutine() {
        println("${this@ImagesFragment::class.java.simpleName} firstLaunchInitDataCoroutine()")
    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        println("${this@ImagesFragment::class.java.simpleName} bindContentViewCoroutine()")
    }

    override fun onResume() {
        super.onResume()
        println("${this@ImagesFragment::class.java.simpleName} onResume()")
    }

    override fun onPause() {
        super.onPause()
        println("${this@ImagesFragment::class.java.simpleName} onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("${this@ImagesFragment::class.java.simpleName} onDestroy()")
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        println("${this::class.java.simpleName} onViewModelCleared()")
    }
}