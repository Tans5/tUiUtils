package com.tans.tuiutils.adapter

import android.view.View
import android.view.ViewGroup

interface ItemViewCreator<Data : Any> : AdapterBuilderLife<Data> {

    var canHandleItemViewTypes : MutableSet<Int>

    fun getItemViewType(positionInDataSource: Int, data: Data): Int?

    fun canHandleViewType(itemViewType: Int): Boolean

    fun createItemView(parent: ViewGroup, itemViewType: Int): View
}