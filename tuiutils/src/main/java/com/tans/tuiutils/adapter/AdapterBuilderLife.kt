package com.tans.tuiutils.adapter

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.Internal

@Internal
interface AdapterBuilderLife<Data : Any> {

    var attachedBuilder: AdapterBuilder<Data>?

    var attachedRecyclerView: RecyclerView?

    fun onAttachToBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        attachedRecyclerView = recyclerView
        attachedBuilder = builder
    }

    fun onDetachedFromBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        attachedRecyclerView = null
        attachedBuilder = null
    }
}