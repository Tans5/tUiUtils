package com.tans.tuiutils.adapter

import androidx.recyclerview.widget.RecyclerView

interface AdapterLifecycle {

    var attachedRecyclerView: RecyclerView?

    fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = recyclerView
    }

    fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = null
    }
}