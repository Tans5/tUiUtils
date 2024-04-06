package com.tans.tuiutils.adapter.impl.viewcreatators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.ItemViewCreator
import com.tans.tuiutils.Internal

class MultipleItemViewCreatorImpl<Data : Any>(
    private val itemViewTypeToLayoutResId: Map<Int, Int>,
    private val getItemViewTypeParam: (positionInDataSource: Int, data: Data) -> Int,
) : ItemViewCreator<Data> {

    init {
        if (itemViewTypeToLayoutResId.isEmpty()) {
            error("itemViewTypeToLayoutResId is empty")
        }
    }
    @Internal
    override fun getItemViewType(positionInDataSource: Int, data: Data): Int = getItemViewTypeParam(positionInDataSource, data)

    @Internal
    override fun createItemView(parent: ViewGroup, itemViewType: Int): View {
        val layoutResId = itemViewTypeToLayoutResId[itemViewType]
        if (layoutResId == null) {
            error("Can't find layoutResId for itemViewType($itemViewType)")
        } else {
            return LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        }
    }

    @Internal
    override var attachedBuilder: AdapterBuilder<Data>? = null

    @Internal
    override var attachedRecyclerView: RecyclerView? = null


}