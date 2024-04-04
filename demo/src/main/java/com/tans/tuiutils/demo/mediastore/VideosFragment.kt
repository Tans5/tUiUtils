package com.tans.tuiutils.demo.mediastore

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tans.tuiutils.adapter.impl.builders.SimpleAdapterBuilderImpl
import com.tans.tuiutils.adapter.impl.builders.plus
import com.tans.tuiutils.adapter.impl.databinders.DataBinderImpl
import com.tans.tuiutils.adapter.impl.datasources.FlowDataSourceImpl
import com.tans.tuiutils.adapter.impl.viewcreatators.SingleItemViewCreatorImpl
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.demo.databinding.EmptyContentLayoutBinding
import com.tans.tuiutils.demo.databinding.FragmentVideosBinding
import com.tans.tuiutils.demo.databinding.VideoItemLayoutBinding
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import com.tans.tuiutils.mediastore.MediaStoreVideo
import com.tans.tuiutils.mediastore.queryVideoFromMediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
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
        val videosAdapterBuilder = SimpleAdapterBuilderImpl<MediaStoreVideo>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.video_item_layout),
            dataSource = FlowDataSourceImpl(stateFlow.map { it.videos }),
            dataBinder = DataBinderImpl { data, view, _ ->
                val itemViewBinding = VideoItemLayoutBinding.bind(view)
                itemViewBinding.videoTitleTv.text = data.title
            }
        )
        val emptyAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.empty_content_layout),
            dataSource = FlowDataSourceImpl(stateFlow.map { if (it.videos.isEmpty()) listOf(Unit) else emptyList() }),
            dataBinder = DataBinderImpl { _, view, _ ->
                val itemViewBinding = EmptyContentLayoutBinding.bind(view)
                itemViewBinding.msgTv.text = "No Video."
            }
        )
        viewBinding.videosRv.adapter = (videosAdapterBuilder + emptyAdapterBuilder).build()

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.videosRv) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
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