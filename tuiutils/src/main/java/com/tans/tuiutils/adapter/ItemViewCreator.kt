package com.tans.tuiutils.adapter

import android.view.View
import android.view.ViewGroup
import com.tans.tuiutils.Internal

@Internal
interface ItemViewCreator<Data : Any> : AdapterBuilderLife<Data> {


    fun getItemViewType(positionInDataSource: Int, data: Data): Int?

    fun createItemView(parent: ViewGroup, itemViewType: Int): View
}