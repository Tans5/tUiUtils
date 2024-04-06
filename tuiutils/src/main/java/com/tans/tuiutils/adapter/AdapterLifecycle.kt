package com.tans.tuiutils.adapter

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.Internal

@Internal
interface AdapterLifecycle<Data : Any> {

    var attachedRecyclerView: RecyclerView?
    var dataSourceParent: DataSourceParent<Data>?

    fun onAttachedToRecyclerView(recyclerView: RecyclerView, dataSourceParent: DataSourceParent<Data>) {
        attachedRecyclerView = recyclerView
        this.dataSourceParent = dataSourceParent
    }

    fun onDetachedFromRecyclerView(recyclerView: RecyclerView, dataSourceParent: DataSourceParent<Data>) {
        attachedRecyclerView = null
        this.dataSourceParent = null
    }
}