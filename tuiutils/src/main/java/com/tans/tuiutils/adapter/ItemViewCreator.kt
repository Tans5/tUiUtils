package com.tans.tuiutils.adapter

interface ItemViewCreator<Data : Any> : AdapterBuilderLife<Data> {

    fun getItemViewType(positionInDataSource: Int, data: Data): Int

    fun createItemView(itemViewType: Int): Int
}