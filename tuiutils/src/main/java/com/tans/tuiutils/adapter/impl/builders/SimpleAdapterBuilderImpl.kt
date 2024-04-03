package com.tans.tuiutils.adapter.impl.builders

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataBinder
import com.tans.tuiutils.adapter.DataSource
import com.tans.tuiutils.adapter.DataSourceParent
import com.tans.tuiutils.adapter.ItemViewCreator

class SimpleAdapterBuilderImpl<Data : Any>(
    override val itemViewCreator: ItemViewCreator<Data>,
    override val dataSource: DataSource<Data>,
    override val dataBinder: DataBinder<Data>,
) : AdapterBuilder<Data> {

    override var isBuilderConsumed: Boolean = false
    override fun build(): RecyclerView.Adapter<*> {
        return SimpleAdapterImpl(this)
    }

    override var attachedRecyclerView: RecyclerView? = null
    override var dataSourceParent: DataSourceParent<Data>? = null
}