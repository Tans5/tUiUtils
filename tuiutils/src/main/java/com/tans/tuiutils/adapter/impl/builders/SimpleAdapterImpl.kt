package com.tans.tuiutils.adapter.impl.builders

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataSource
import com.tans.tuiutils.adapter.DataSourceParent
import com.tans.tuiutils.tUiUtilsLog

internal class SimpleAdapterImpl<Data : Any>(
    private val adapterBuilder: AdapterBuilder<Data>
) : ListAdapter<Data, ViewHolder> (
    object : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return adapterBuilder.dataSource.areDataItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return adapterBuilder.dataSource.areDataItemsContentTheSame(oldItem, newItem)
        }

        override fun getChangePayload(oldItem: Data, newItem: Data): Any? {
            return adapterBuilder.dataSource.getDataItemsChangePayload(oldItem, newItem)
        }
    }
), DataSourceParent<Data> {

    init {
        adapterBuilder.consumeBuilder()
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        adapterBuilder.onAttachedToRecyclerView(recyclerView, this)
    }

    override fun getItemId(position: Int): Long {
        val data = adapterBuilder.dataSource.getLastSubmittedData(position) ?: error("Wrong data position: $position")
        return adapterBuilder.dataSource.getDataItemId(data = data, positionInDataSource = position)
    }

    override fun getItemViewType(position: Int): Int {
        val data = adapterBuilder.dataSource.getLastSubmittedData(position) ?: error("Wrong data position: $position")
        return adapterBuilder.itemViewCreator.getItemViewType(
            positionInDataSource = position,
            data = data
        ) ?: super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = adapterBuilder.itemViewCreator.createItemView(parent, viewType)
        return object : ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val data = adapterBuilder.dataSource.getLastSubmittedData(position) ?: error("Wrong data position: $position")
            adapterBuilder.dataBinder.bindPayloadData(data = data, view = holder.itemView, positionInDataSource = position, payloads = payloads)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = adapterBuilder.dataSource.getLastSubmittedData(position) ?: error("Wrong data position: $position")
        adapterBuilder.dataBinder.bindData(data = data, view = holder.itemView, positionInDataSource = position)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        adapterBuilder.onDetachedFromRecyclerView(recyclerView, this)
    }

    override fun requestSubmitDataList(child: DataSource<Data>, data: List<Data>, callback: Runnable?) {
        super.requestSubmitDataList(child, data, callback)
        tUiUtilsLog.d(TAG, "Request submit list count: ${data.size}")
        submitList(data) {
            tUiUtilsLog.d(TAG, "Submitted list count: ${data.size}")
            callback?.run()
        }
    }

    companion object {
        private const val TAG = "SimpleAdapterImpl"
    }
}