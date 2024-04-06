package com.tans.tuiutils.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.tans.tuiutils.Internal

@Internal
interface AdapterBuilder<Data : Any> : AdapterLifecycle<Data>, DataSourceParent<Data> {

    var isBuilderConsumed: Boolean

    val itemViewCreator: ItemViewCreator<Data>

    val dataSource: DataSource<Data>

    val dataBinder: DataBinder<Data>

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView, dataSourceParent: DataSourceParent<Data>) {
        super.onAttachedToRecyclerView(recyclerView, dataSourceParent)
        itemViewCreator.onAttachToBuilder(recyclerView, this)
        dataSource.onAttachToBuilder(recyclerView, this)
        dataBinder.onAttachToBuilder(recyclerView, this)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView, dataSourceParent: DataSourceParent<Data>) {
        super.onDetachedFromRecyclerView(recyclerView, dataSourceParent)
        itemViewCreator.onDetachedFromBuilder(recyclerView, this)
        dataSource.onDetachedFromBuilder(recyclerView, this)
        dataBinder.onDetachedFromBuilder(recyclerView, this)
    }

    override fun requestSubmitDataList(
        child: DataSource<Data>,
        data: List<Data>,
        callback: Runnable?
    ) {
        super.requestSubmitDataList(child, data, callback)
        val parent = dataSourceParent
        if (parent == null) {
            error("Parent DataSource is null.")
        } else {
            parent.requestSubmitDataList(child, data, callback)
        }
    }

    fun consumeBuilder() {
        if (isBuilderConsumed) {
            error("Adapter builder already consumed, please create new AdapterBuilder.")
        } else {
            isBuilderConsumed = true
        }
    }

    fun build(): Adapter<*>
}