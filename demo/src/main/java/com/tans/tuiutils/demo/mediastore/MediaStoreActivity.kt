package com.tans.tuiutils.demo.mediastore

import android.view.View
import androidx.collection.LongSparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.tans.tuiutils.activity.BaseCoroutineStateActivity
import com.tans.tuiutils.demo.R
import com.tans.tuiutils.demo.databinding.ActivityMediaStoreBinding
import com.tans.tuiutils.systembar.annotation.SystemBarStyle
import kotlinx.coroutines.CoroutineScope

@SystemBarStyle
class MediaStoreActivity : BaseCoroutineStateActivity<MediaStoreActivity.Companion.State>(State()) {

    override val layoutId: Int = R.layout.activity_media_store

    private val mediaStoresFragments: Map<MediaTab, Fragment> by lazyViewModelField {
        mapOf(
            MediaTab.Images to ImagesFragment(),
            MediaTab.Audios to AudiosFragment(),
            MediaTab.Videos to VideosFragment()
        )
    }

    override fun CoroutineScope.firstLaunchInitDataCoroutine() {

    }

    override fun CoroutineScope.bindContentViewCoroutine(contentView: View) {
        val viewBinding = ActivityMediaStoreBinding.bind(contentView)
        val fragmentAdapter = object : FragmentStateAdapter(this@MediaStoreActivity) {
            override fun getItemCount(): Int = mediaStoresFragments.size
            override fun createFragment(position: Int): Fragment {
                val key = MediaTab.entries[position]
                return mediaStoresFragments[key]!!
            }
        }
        // Fix relaunch cause view pager crash.
        fragmentAdapterPutFragments(fragmentAdapter)
        viewBinding.viewPager.adapter = fragmentAdapter
        viewBinding.viewPager.offscreenPageLimit = mediaStoresFragments.size
        viewBinding.viewPager.isSaveEnabled = false

        TabLayoutMediator(viewBinding.tabLayout, viewBinding.viewPager) { tab, position ->
            tab.text = MediaTab.entries[position].name
        }.attach()

        viewBinding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    updateState { it.copy(selectedTab = MediaTab.entries[tab.position]) }
                }
            }
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })

        renderStateNewCoroutine({it.selectedTab}) { selectedTab ->
            if (viewBinding.tabLayout.selectedTabPosition != selectedTab.ordinal) {
                viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(selectedTab.ordinal))
            }
        }
    }

    private fun fragmentAdapterPutFragments(adapter: FragmentStateAdapter) {
        try {
            val clazz = FragmentStateAdapter::class.java
            val fragmentsField = clazz.getDeclaredField("mFragments")
            fragmentsField.isAccessible = true
            val fragments = fragmentsField.get(adapter) as LongSparseArray<Fragment>
            fragments.clear()
            for ((i, f) in this.mediaStoresFragments.map { it.value }.withIndex()) {
                fragments.put(i.toLong(), f)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    companion object {
        enum class MediaTab {
            Images, Audios, Videos
        }

        data class State(
            val selectedTab: MediaTab = MediaTab.Images
        )
    }
}