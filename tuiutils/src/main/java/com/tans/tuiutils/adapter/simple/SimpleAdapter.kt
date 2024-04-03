package com.tans.tuiutils.adapter.simple

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataSource
import com.tans.tuiutils.adapter.DataSourceParent

internal class SimpleAdapter<Data : Any>(
    private val adapterBuilder: AdapterBuilder<Data>
) : ListAdapter<Data, ViewHolder> (
    object : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            TODO("Not yet implemented")
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            TODO("Not yet implemented")
        }

        override fun getChangePayload(oldItem: Data, newItem: Data): Any? {
            return super.getChangePayload(oldItem, newItem)
        }
    }
), DataSourceParent<Data> {

    init {
        adapterBuilder.consumeBuilder()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun requestSubmitDataList(child: DataSource<Data>, data: List<Data>, callback: Runnable?) {
        if (callback == null) {
            submitList(data)
        } else {
            submitList(data, callback)
        }
    }
}