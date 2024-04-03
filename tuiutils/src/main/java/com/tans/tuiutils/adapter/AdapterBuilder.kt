package com.tans.tuiutils.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

interface AdapterBuilder<Data : Any> : AdapterLifecycle {

    var isBuilderConsumed: Boolean

    val itemViewCreator: ItemViewCreator<Data>

    val dataSource: DataSource<Data>

    val dataBinder: DataBinder<Data>

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemViewCreator.onAttachToBuilder(recyclerView, this)
        dataSource.onAttachToBuilder(recyclerView, this)
        dataBinder.onAttachToBuilder(recyclerView, this)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemViewCreator.onDetachedFromBuilder(recyclerView, this)
        dataSource.onDetachedFromBuilder(recyclerView, this)
        dataBinder.onDetachedFromBuilder(recyclerView, this)
    }

    fun consumeBuilder() {
        if (isBuilderConsumed) {
            error("Adapter builder already consumed, please create new AdapterBuilder")
        } else {
            isBuilderConsumed = true
        }
    }

    fun build(): Adapter<*>
}