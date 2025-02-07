package com.tans.tuiutils.demo

import android.app.Dialog
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tans.tuiutils.adapter.impl.builders.SimpleAdapterBuilderImpl
import com.tans.tuiutils.adapter.impl.builders.plus
import com.tans.tuiutils.adapter.impl.databinders.DataBinderImpl
import com.tans.tuiutils.adapter.impl.datasources.FlowDataSourceImpl
import com.tans.tuiutils.adapter.impl.viewcreatators.SingleItemViewCreatorImpl
import com.tans.tuiutils.view.clicks
import com.tans.tuiutils.demo.databinding.AudioSelectItemLayoutBinding
import com.tans.tuiutils.demo.databinding.DialogSelectAudioBinding
import com.tans.tuiutils.demo.databinding.EmptyContentLayoutBinding
import com.tans.tuiutils.dialog.BaseSimpleCoroutineResultCancelableDialogFragment
import com.tans.tuiutils.dialog.createBottomSheetDialog
import com.tans.tuiutils.mediastore.MediaStoreAudio
import com.tans.tuiutils.mediastore.queryAudioFromMediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioSelectDialog : BaseSimpleCoroutineResultCancelableDialogFragment<AudioSelectDialog.Companion.State, List<MediaStoreAudio>>(s = State()) {

    override val contentViewHeightInScreenRatio: Float = 1.0f

    override val layoutId: Int = R.layout.dialog_select_audio

    override fun firstLaunchInitData() {
        launch(Dispatchers.IO) {
            val audios = this@AudioSelectDialog.queryAudioFromMediaStore()
            updateState {
                it.copy(audiosWithSelected = audios.map { a -> AudioWithSelected(a, false) }, hasLoadFirstData = true)
            }
        }
    }

    override fun bindContentView(view: View) {
        val viewBinding = DialogSelectAudioBinding.bind(view)
        val contentAdapterBuilder = SimpleAdapterBuilderImpl<AudioWithSelected>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.audio_select_item_layout),
            dataSource = FlowDataSourceImpl(
                dataFlow = stateFlow.map { it.audiosWithSelected },
                areDataItemsTheSameParam = { d1, d2 -> d1.audio.id == d2.audio.id },
                getDataItemsChangePayloadParam = { d1, d2 ->
                    if (d1.audio == d2.audio && d1.isSelected != d2.isSelected) {
                        Unit
                    } else {
                        null
                    }
                },
                getDataItemIdParam = { data, _ -> data.audio.id }
            ),
            dataBinder = DataBinderImpl<AudioWithSelected> { data, itemView, _ ->
                val itemViewBinding = AudioSelectItemLayoutBinding.bind(itemView)
                itemViewBinding.musicTitleTv.text = data.audio.title
                itemViewBinding.artistAlbumTv.text = "${data.audio.artist}-${data.audio.album}"
                itemViewBinding.root.clicks(this@AudioSelectDialog) {
                    withContext(Dispatchers.IO) {
                        val audio = data.audio
                        updateState { oldState ->
                            oldState.copy(audiosWithSelected = oldState.audiosWithSelected
                                .map { if (it.audio == audio) it.copy(isSelected = !it.isSelected) else it }
                            )
                        }
                    }
                }
            }.addPayloadDataBinder(Unit) { data, itemView, _ ->
                val itemViewBinding = AudioSelectItemLayoutBinding.bind(itemView)
                itemViewBinding.selectCb.isChecked = data.isSelected
            }
        )
        val emptyAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.empty_content_layout),
            dataSource = FlowDataSourceImpl(stateFlow.map { if (it.audiosWithSelected.isEmpty() && it.hasLoadFirstData) listOf(Unit) else emptyList() }),
            dataBinder = DataBinderImpl{ _, itemView, _ ->
                val itemViewBinding = EmptyContentLayoutBinding.bind(itemView)
                itemViewBinding.msgTv.text = "No Audio."
            }
        )
        viewBinding.audiosRv.adapter = (contentAdapterBuilder + emptyAdapterBuilder).build()

        viewBinding.cancelBt.clicks(this) {
            onCancel()
        }

        viewBinding.finishBt.clicks(this) {
            val selectedAudios = currentState().audiosWithSelected.filter { it.isSelected }.map { it.audio }
            if (selectedAudios.isNotEmpty()) {
                onResult(selectedAudios)
            } else {
                onCancel()
            }
        }
    }

    override fun createDialog(contentView: View): Dialog {
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { v, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemInsets.bottom + systemInsets.top)
            insets
        }
        return requireActivity().createBottomSheetDialog(contentView = contentView) { b ->
            b.isDraggable = true
            b.isHideable = true
        }
    }

    companion object {

        data class AudioWithSelected(
            val audio: MediaStoreAudio,
            val isSelected: Boolean
        )

        data class State(
            val audiosWithSelected: List<AudioWithSelected> = emptyList(),
            val hasLoadFirstData: Boolean = false
        )
    }
}