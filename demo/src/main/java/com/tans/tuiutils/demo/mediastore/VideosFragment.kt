package com.tans.tuiutils.demo.mediastore

import android.os.Bundle
import android.view.View
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.demo.databinding.FragmentVideosBinding
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import com.tans.tuiutils.mediastore.MediaStoreVideo
import com.tans.tuiutils.mediastore.queryVideoFromMediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class VideosFragment : BaseCoroutineStateFragment<VideosFragment.Companion.State>(State()) {

    override val layoutId: Int = R.layout.fragment_videos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this@VideosFragment::class.java.simpleName} onCreate(): $savedInstanceState")
    }

    override fun CoroutineScope.firstLaunchInitDataCoroutine() {
        println("${this@VideosFragment::class.java.simpleName} firstLaunchInitDataCoroutine()")
        launch {
            val videos = this@VideosFragment.queryVideoFromMediaStore()
            updateState { it.copy(videos = videos) }
        }
    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        val viewBinding = FragmentVideosBinding.bind(contentView)
        println("${this@VideosFragment::class.java.simpleName} bindContentViewCoroutine()")
        renderStateNewCoroutine({ it.videos }) {
            viewBinding.videosCountTv.text = "Video Count: ${it.size}"
        }
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

    companion object {
        data class State(val videos: List<MediaStoreVideo> = emptyList())
    }
}