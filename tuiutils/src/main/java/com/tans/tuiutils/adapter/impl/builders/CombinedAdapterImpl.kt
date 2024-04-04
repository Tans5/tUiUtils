package com.tans.tuiutils.adapter.impl.builders

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataSource
import com.tans.tuiutils.adapter.DataSourceParent
import com.tans.tuiutils.tUiUtilsLog

internal class CombinedAdapterImpl(private val combinedAdapterBuilder: CombinedAdapterBuilderImpl) : ListAdapter<Any, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Any>() {

        private fun printDataTypeAndSize() {
            for (c in combinedAdapterBuilder.childrenBuilders) {
                tUiUtilsLog.e(TAG, "DataClass=${c.dataSource.tryGetDataClass()}, DataCount=${c.dataSource.getLastSubmittedDataSize()}")
            }
        }

        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            val classA = oldItem::class.java
            val classB = newItem::class.java
            return if (classA !== classB) {
                false
            } else {
                val builder = combinedAdapterBuilder.childrenBuilders.find { it.dataSource.tryGetDataClass() === classA }
                if (builder == null) {
                    tUiUtilsLog.e(TAG, "Can't find datasource contain data class: $classA")
                    printDataTypeAndSize()
                    oldItem == newItem
                } else {
                    builder.dataSource.areDataItemsTheSame(oldItem, newItem)
                }
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            val classA = oldItem::class.java
            val classB = newItem::class.java
            return if (classA !== classB) {
                false
            } else {
                val builder = combinedAdapterBuilder.childrenBuilders.find { it.dataSource.tryGetDataClass() === classA }
                if (builder == null) {
                    tUiUtilsLog.e(TAG, "Can't find datasource contain data class: $classA")
                    printDataTypeAndSize()
                    oldItem == newItem
                } else {
                    builder.dataSource.areDataItemsContentTheSame(oldItem, newItem)
                }
            }
        }

        override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
            val classA = oldItem::class.java
            val classB = newItem::class.java
            return if (classA !== classB) {
                null
            } else {
                val builder = combinedAdapterBuilder.childrenBuilders.find { it.dataSource.tryGetDataClass() === classA }
                if (builder == null) {
                    tUiUtilsLog.e(TAG, "Can't find datasource contain data class: $classA")
                    printDataTypeAndSize()
                    null
                } else {
                    builder.dataSource.getDataItemsChangePayload(oldItem, newItem)
                }
            }
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

    override fun getItemId(position: Int): Long {
        val (builder, positionInDataSource, _) = findTargetBuilderByAdapterPosition(
            position
        )
        val data = builder.dataSource.getLastSubmittedData(positionInDataSource) ?: error("Can't get data for positionInDataSource=$positionInDataSource")
        return builder.dataSource.getDataItemId(data, positionInDataSource)
    }

    override fun getItemViewType(position: Int): Int {
        val (builder, positionInDataSource, builderIndex) = findTargetBuilderByAdapterPosition(
            position
        )
        if (builderIndex > MAX_BUILDER_INDEX) {
            error("BuilderIndex($builderIndex) great than MaxBuilderIndex($MAX_BUILDER_INDEX)")
        }
        val builderItemViewType = builder.itemViewCreator.getItemViewType(
            positionInDataSource = positionInDataSource,
            data = builder.dataSource.getLastSubmittedData(positionInDataSource)
                ?: error("Wrong data position: $positionInDataSource")
        ) ?: super.getItemViewType(position)
        if (builderItemViewType > MAX_ITEM_VIEW_TYPE) {
            error("ItemViewType($builderItemViewType) great than MaxItemViewType($MAX_ITEM_VIEW_TYPE)")
        }
        val viewTypeFix = builderItemViewType and BUILDER_ITEM_VIEW_MASK
        val builderIndexFix = builderIndex.shl(BUILDER_INDEX_MASK_OFFSET_BIT)
        return builderIndexFix or viewTypeFix
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewTypeFix = viewType and BUILDER_ITEM_VIEW_MASK
        val builderIndexFix = (viewType and BUILDER_INDEX_MASK).ushr(BUILDER_INDEX_MASK_OFFSET_BIT)
        val builder = childrenBuilders.getOrNull(builderIndexFix) ?: error("Can't get builder for index=${builderIndexFix}, builderSize=${childrenBuilders.size}")
        val itemView = builder.itemViewCreator.createItemView(
            parent = parent,
            itemViewType = viewTypeFix
        )
        return object : RecyclerView.ViewHolder(itemView) {}
    }


    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val (builder, positionInDataSource, _) = findTargetBuilderByAdapterPosition(position)
            val data = builder.dataSource.getLastSubmittedData(positionInDataSource) ?: error("Can't get data for positionInDataSource=$positionInDataSource")
            builder.dataBinder.bindPayloadData(
                data = data,
                view = holder.itemView,
                positionInDataSource = positionInDataSource,
                payloads = payloads
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (builder, positionInDataSource, _) = findTargetBuilderByAdapterPosition(position)
        val data = builder.dataSource.getLastSubmittedData(positionInDataSource) ?: error("Can't get data for positionInDataSource=$positionInDataSource")
        builder.dataBinder.bindData(
            data = data,
            view = holder.itemView,
            positionInDataSource = positionInDataSource
        )
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
                val append = c.dataSource.lastSubmittedDataList
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

    /**
     * @return First: AdapterBuilder, Second: position in data source, Third: index of adapter builder.
     */
    private fun findTargetBuilderByAdapterPosition(adapterPosition: Int): Triple<AdapterBuilder<Any>, Int, Int> {
        var startIndex = 0
        var targetBuilder: AdapterBuilder<Any>? = null
        var builderIndex = 0
        for (c in childrenBuilders) {
            val ds = c.dataSource.getLastSubmittedDataSize()
            if (adapterPosition in startIndex until (startIndex + ds)) {
                targetBuilder = c
                break
            } else {
                startIndex += ds
            }
            builderIndex ++
        }
        return if (targetBuilder != null) {
            Triple(targetBuilder, adapterPosition - startIndex, builderIndex)
        } else {
            error("Can't handle adapter position: $adapterPosition")
        }
    }

    companion object {
        private const val BUILDER_ITEM_VIEW_MASK: Int = 0x00_00_FF_FF
        private const val BUILDER_INDEX_MASK: Int = 0xFF_FF_00_00.toInt()
        private const val BUILDER_INDEX_MASK_OFFSET_BIT = 16
        private const val MAX_BUILDER_INDEX = 65535
        private const val MAX_ITEM_VIEW_TYPE = 65535
        private const val TAG = "CombinedAdapterImpl"
    }
}