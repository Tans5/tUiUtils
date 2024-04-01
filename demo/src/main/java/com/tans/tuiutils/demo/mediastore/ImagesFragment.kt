package com.tans.tuiutils.demo.mediastore

import android.os.Bundle
import android.view.View
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.demo.databinding.FragmentImagesBinding
import com.tans.tuiutils.fragment.BaseCoroutineStateFragment
import com.tans.tuiutils.mediastore.MediaStoreImage
import com.tans.tuiutils.mediastore.queryImageFromMediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ImagesFragment : BaseCoroutineStateFragment<ImagesFragment.Companion.State>(State()) {

    override val layoutId: Int = R.layout.fragment_images

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this@ImagesFragment::class.java.simpleName} onCreate(): $savedInstanceState")
    }

    override fun CoroutineScope.firstLaunchInitDataCoroutine() {
        println("${this@ImagesFragment::class.java.simpleName} firstLaunchInitDataCoroutine()")
        launch {
            val images = this@ImagesFragment.queryImageFromMediaStore()
            updateState {
                it.copy(images = images)
            }
        }
    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        println("${this@ImagesFragment::class.java.simpleName} bindContentViewCoroutine()")
        val viewBinding = FragmentImagesBinding.bind(contentView)
        renderStateNewCoroutine({ it.images }) {
            viewBinding.imageCountTv.text = "Image Count: ${it.size}"
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