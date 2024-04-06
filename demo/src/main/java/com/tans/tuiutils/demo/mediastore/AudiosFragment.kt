package com.tans.tuiutils.demo.mediastore

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tans.tuiutils.adapter.decoration.MarginDividerItemDecoration
import com.tans.tuiutils.adapter.decoration.ignoreLastDividerController
import com.tans.tuiutils.adapter.impl.builders.SimpleAdapterBuilderImpl
import com.tans.tuiutils.adapter.impl.builders.plus
import com.tans.tuiutils.adapter.impl.databinders.DataBinderImpl
import com.tans.tuiutils.adapter.impl.datasources.FlowDataSourceImpl
import com.tans.tuiutils.adapter.impl.viewcreatators.SingleItemViewCreatorImpl
import com.tans.tuiutils.view.clicks
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.demo.databinding.AudioItemLayoutBinding
import com.tans.tuiutils.demo.databinding.EmptyContentLayoutBinding
import com.tans.tuiutils.demo.databinding.FragmentAudiosBinding
import com.tans.tuiutils.demo.databinding.HeaderFooterItemLayoutBinding
import com.tans.tuiutils.dialog.dp2px
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import com.tans.tuiutils.mediastore.MediaStoreAudio
import com.tans.tuiutils.mediastore.queryAudioFromMediaStore
import com.tans.tuiutils.view.refreshes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AudiosFragment : BaseCoroutineStateFragment<AudiosFragment.Companion.State>(State()) {

    override val layoutId: Int = R.layout.fragment_audios

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this@AudiosFragment::class.java.simpleName} onCreate(): $savedInstanceState")
    }

    override fun CoroutineScope.firstLaunchInitDataCoroutine() {
        println("${this@AudiosFragment::class.java.simpleName} firstLaunchInitDataCoroutine()")
        launch {
            val audios = this@AudiosFragment.queryAudioFromMediaStore()
            updateState { it.copy(audios = audios, hasLoadFirstData = true) }
        }
    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        println("${this@AudiosFragment::class.java.simpleName} bindContentViewCoroutine()")
        val viewBinding = FragmentAudiosBinding.bind(contentView)

        val headerFooterDataFlow = stateFlow
            .map { if (it.audios.isEmpty()) emptyList() else listOf(Unit) }

        // Header
        val headerAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.header_footer_item_layout),
            dataSource = FlowDataSourceImpl(headerFooterDataFlow),
            dataBinder = DataBinderImpl { _, view, _ ->
                HeaderFooterItemLayoutBinding.bind(view).let { binding ->
                    binding.msgTv.text = "Header"
                }
            }
        )

        // Audios Content
        val audiosAdapterBuilder = SimpleAdapterBuilderImpl<MediaStoreAudio>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.audio_item_layout),
            dataSource = FlowDataSourceImpl(stateFlow.map { it.audios }),
            dataBinder = DataBinderImpl { data, view, _ ->
                val itemViewBinding = AudioItemLayoutBinding.bind(view)
                itemViewBinding.musicTitleTv.text = data.title
                itemViewBinding.artistAlbumTv.text = "${data.artist}-${data.album}"
                view.clicks(this) {
                    Toast.makeText(this@AudiosFragment.requireContext(), data.title, Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Footer
        val footerAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.header_footer_item_layout),
            dataSource = FlowDataSourceImpl(headerFooterDataFlow),
            dataBinder = DataBinderImpl { _, view, _ ->
                HeaderFooterItemLayoutBinding.bind(view).let { binding ->
                    binding.msgTv.text = "Footer"
                }
            }
        )

        // Empty
        val emptyAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.empty_content_layout),
            dataSource = FlowDataSourceImpl(stateFlow.map { if (it.audios.isEmpty() && it.hasLoadFirstData) listOf(Unit) else emptyList() }),
            dataBinder = DataBinderImpl { _, view, _ ->
                EmptyContentLayoutBinding.bind(view).let { binding ->
                    binding.msgTv.text = "No Audio."
                }
            }
        )
        viewBinding.audiosRv.adapter = (headerAdapterBuilder + audiosAdapterBuilder + footerAdapterBuilder + emptyAdapterBuilder).build()

        viewBinding.audiosRv.addItemDecoration(
            MarginDividerItemDecoration.Companion.Builder()
                .divider(MarginDividerItemDecoration.Companion.ColorDivider(color = Color.parseColor("#F2F2F2"), size = requireContext().dp2px(1)))
                .dividerDirection(MarginDividerItemDecoration.Companion.DividerDirection.Horizontal)
                .marginStart(requireContext().dp2px(16))
                .dividerController(ignoreLastDividerController)
                .build()
        )
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.audiosRv) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        viewBinding.swipeRefresh.refreshes(this, Dispatchers.IO) {
            delay(500)
            val audios = this@AudiosFragment.queryAudioFromMediaStore()
            updateState { it.copy(audios = audios) }
        }
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

    companion object {
        data class State(val audios: List<MediaStoreAudio> = emptyList(), val hasLoadFirstData: Boolean = false)
    }
}