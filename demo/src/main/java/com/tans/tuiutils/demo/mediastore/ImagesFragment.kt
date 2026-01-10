package com.tans.tuiutils.demo.mediastore

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.tans.tuiutils.adapter.impl.builders.SimpleAdapterBuilderImpl
import com.tans.tuiutils.adapter.impl.databinders.DataBinderImpl
import com.tans.tuiutils.adapter.impl.datasources.FlowDataSourceImpl
import com.tans.tuiutils.adapter.impl.viewcreatators.SingleItemViewCreatorImpl
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.demo.databinding.FragmentImagesBinding
import com.tans.tuiutils.demo.databinding.ImageItemLayoutBinding
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import com.tans.tuiutils.mediastore.MediaStoreImage
import com.tans.tuiutils.mediastore.queryImageFromMediaStore
import com.tans.tuiutils.state.Action
import com.tans.tuiutils.view.clicks
import com.tans.tuiutils.view.refreshes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

class ImagesFragment : BaseCoroutineStateFragment<ImagesFragment.Companion.State>(State()) {

    override val layoutId: Int = R.layout.fragment_images

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this@ImagesFragment::class.java.simpleName} onCreate(): $savedInstanceState")
    }

    override fun firstLaunchInitData(savedInstanceState: Bundle?) {
        println("${this@ImagesFragment::class.java.simpleName} firstLaunchInitDataCoroutine(): ${this.hashCode()}")
        enqueueAction(object : Action<State>() {
            override suspend fun execute(oldState: State): State {
                val images = this@ImagesFragment.queryImageFromMediaStore()
                return oldState.copy(images = images)
            }
        })
    }

    override fun bindContentView(
        contentView: View,
        useLastContentView: Boolean
    ) {
        println("${this@ImagesFragment::class.java.simpleName} bindContentViewCoroutine()")
        val viewBinding = FragmentImagesBinding.bind(contentView)
        val adapterBuilder = SimpleAdapterBuilderImpl<MediaStoreImage>(
            itemViewCreator = SingleItemViewCreatorImpl(R.layout.image_item_layout),
            dataSource = FlowDataSourceImpl(lifecycleScope, stateFlow.map { it.images }),
            dataBinder = DataBinderImpl { data, view, _ ->
                val itemViewBinding = ImageItemLayoutBinding.bind(view)
                context?.let {
                    Glide.with(it)
                        .load(data.uri)
                        .into(itemViewBinding.imageIv)
                }
                itemViewBinding.root.clicks(lifecycleScope) {
                    Toast.makeText(this@ImagesFragment.requireContext(), data.title, Toast.LENGTH_SHORT).show()
                }
            }
        )
        viewBinding.imagesRv.adapter = adapterBuilder.build()
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.imagesRv) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        viewBinding.swipeRefresh.refreshes(lifecycleScope, Dispatchers.IO) {
            delay(500)
            val images = this@ImagesFragment.queryImageFromMediaStore()
            updateState { it.copy(images = images) }
        }
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

    companion object {
        data class State(val images: List<MediaStoreImage> = emptyList())
    }
}