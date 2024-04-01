package com.tans.tuiutils.demo.mediastore

import android.view.View
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import kotlinx.coroutines.CoroutineScope

class VideosFragment : BaseCoroutineStateFragment<Unit>(Unit) {

    override val layoutId: Int = R.layout.fragment_videos
    override fun CoroutineScope.firstLaunchInitDataCoroutine() {
        println("${this@VideosFragment::class.java.simpleName} firstLaunchInitDataCoroutine()")
    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        println("${this@VideosFragment::class.java.simpleName} bindContentViewCoroutine()")
    }

    override fun onResume() {
        super.onResume()
        println("${this@VideosFragment::class.java.simpleName} onResume()")
    }

    override fun onPause() {
        super.onPause()
        println("${this@VideosFragment::class.java.simpleName} onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("${this@VideosFragment::class.java.simpleName} onDestroy()")
    }
}