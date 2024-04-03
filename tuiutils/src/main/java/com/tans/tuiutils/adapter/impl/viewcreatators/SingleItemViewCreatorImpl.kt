package com.tans.tuiutils.adapter.impl.viewcreatators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.ItemViewCreator

class SingleItemViewCreatorImpl<Data : Any>(
    @LayoutRes
    private val itemViewLayoutRes: Int
) : ItemViewCreator<Data> {

    override var canHandleItemViewTypes: MutableSet<Int> = mutableSetOf()

    override fun getItemViewType(positionInDataSource: Int, data: Data): Int? = null

    override fun canHandleViewType(itemViewType: Int): Boolean {
        return true
    }

    override fun createItemView(parent: ViewGroup, itemViewType: Int): View {
      return LayoutInflater.from(parent.context).inflate(itemViewLayoutRes, parent, false)
    }

    override var attachedBuilder: AdapterBuilder<Data>? = null

    override var attachedRecyclerView: RecyclerView? = null

}