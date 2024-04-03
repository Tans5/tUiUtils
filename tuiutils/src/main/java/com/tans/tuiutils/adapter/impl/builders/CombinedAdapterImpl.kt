package com.tans.tuiutils.adapter.impl.builders

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataSource
import com.tans.tuiutils.adapter.DataSourceParent

class CombinedAdapterImpl(private val combinedAdapterBuilder: CombinedAdapterBuilderImpl) : ListAdapter<Any, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            TODO("Not yet implemented")
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            TODO("Not yet implemented")
        }
    }
), DataSourceParent<Any> {
    init {
        val builders = combinedAdapterBuilder.childrenBuilders
        if (builders.isEmpty()) {
            error("CombinedAdapterBuilderImpl#childrenBuilders can't be empty.")
        } else {
            for (b in builders) {
                b.consumeBuilder()
            }
        }
    }

    private val childrenBuilders by lazy {
        combinedAdapterBuilder.childrenBuilders
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        for (c in childrenBuilders) {
            c.onAttachedToRecyclerView(recyclerView, this)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val (builder, positionInDataSource) = findTargetBuilderByAdapterPosition(position)
        val itemViewType = builder.itemViewCreator.getItemViewType(positionInDataSource, builder.dataSource.getLastSubmittedData(positionInDataSource) ?: error("Wrong data position: $positionInDataSource"))
        return itemViewType ?: super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        for (c in childrenBuilders) {
            c.onDetachedFromRecyclerView(recyclerView, this)
        }
    }

    override fun requestSubmitDataList(
        child: DataSource<Any>,
        data: List<Any>,
        callback: Runnable?
    ) {
        super.requestSubmitDataList(child, data, callback)
        val combinedList = mutableListOf<Any>()
        for (c in childrenBuilders) {
            if (c.dataSource === child) {
                combinedList.addAll(data)
            } else {
                val append = c.dataSource.submittingDataList ?: c.dataSource.lastSubmittedDataList
                if (append != null) {
                    combinedList.addAll(append)
                }
            }
        }
        if (callback == null) {
            submitList(combinedList)
        } else {
            submitList(combinedList, callback)
        }
    }

    private fun findTargetBuilderByAdapterPosition(adapterPosition: Int): Pair<AdapterBuilder<Any>, Int> {
        var startIndex = 0
        var targetBuilder: AdapterBuilder<Any>? = null
        for (c in childrenBuilders) {
            val ds = c.dataSource.getLastSubmittedDataSize()
            if (adapterPosition in startIndex until ds) {
                targetBuilder = c
            } else {
                startIndex += ds
            }
        }
        return if (targetBuilder != null) {
            targetBuilder to adapterPosition - startIndex
        } else {
            error("Can't handle adapter position: $adapterPosition")
        }
    }
}