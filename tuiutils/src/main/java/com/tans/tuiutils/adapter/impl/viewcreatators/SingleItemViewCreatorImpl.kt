package com.tans.tuiutils.adapter.impl.viewcreatators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.ItemViewCreator
import org.jetbrains.annotations.ApiStatus.Internal

class SingleItemViewCreatorImpl<Data : Any>(
    @LayoutRes
    private val itemViewLayoutRes: Int
) : ItemViewCreator<Data> {

    @Internal
    override fun getItemViewType(positionInDataSource: Int, data: Data): Int? = null

    @Internal
    override fun createItemView(parent: ViewGroup, itemViewType: Int): View {
      return LayoutInflater.from(parent.context).inflate(itemViewLayoutRes, parent, false)
    }

    @Internal
    override var attachedBuilder: AdapterBuilder<Data>? = null

    @Internal
    override var attachedRecyclerView: RecyclerView? = null

}