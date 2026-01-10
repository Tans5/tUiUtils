package com.tans.tuiutils.adapter.impl.builders

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataSource
import com.tans.tuiutils.adapter.DataSourceParent
import com.tans.tuiutils.tUiUtilsLog

internal class CombinedAdapterImpl(private val combinedAdapterBuilder: CombinedAdapterBuilderImpl) : ListAdapter<CombinedAdapterImpl.Companion.CombinedItemWrapper, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<CombinedItemWrapper>() {

        override fun areItemsTheSame(oldItem: CombinedItemWrapper, newItem: CombinedItemWrapper): Boolean {
            return if (oldItem.builderIndex != newItem.builderIndex) {
                false
            } else {
                val builder = combinedAdapterBuilder.childrenBuilders.getOrNull(oldItem.builderIndex)
                if (builder == null) {
                    tUiUtilsLog.e(TAG, "Can't find builder for index: ${oldItem.builderIndex}")
                    false
                } else {
                    builder.dataSource.areDataItemsTheSame(oldItem.data, newItem.data)
                }
            }
        }

        override fun areContentsTheSame(oldItem: CombinedItemWrapper, newItem: CombinedItemWrapper): Boolean {
            return if (oldItem.builderIndex != newItem.builderIndex) {
                false
            } else {
                val builder = combinedAdapterBuilder.childrenBuilders.getOrNull(oldItem.builderIndex)
                if (builder == null) {
                    tUiUtilsLog.e(TAG, "Can't find builder for index: ${oldItem.builderIndex}")
                    false
                } else {
                    builder.dataSource.areDataItemsContentTheSame(oldItem.data, newItem.data)
                }
            }
        }

        override fun getChangePayload(oldItem: CombinedItemWrapper, newItem: CombinedItemWrapper): Any? {
            return if (oldItem.builderIndex != newItem.builderIndex) {
                null
            } else {
                val builder = combinedAdapterBuilder.childrenBuilders.getOrNull(oldItem.builderIndex)
                if (builder == null) {
                    tUiUtilsLog.e(TAG, "Can't find builder for index: ${oldItem.builderIndex}")
                    null
                } else {
                    builder.dataSource.getDataItemsChangePayload(oldItem.data, newItem.data)
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

    private val childrenRequestSubmitDataTasks: MutableList<ChildRequestSubmitDataTask> = mutableListOf()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        for (c in childrenBuilders) {
            c.onAttachedToRecyclerView(recyclerView, this)
        }
    }

    override fun getItemId(position: Int): Long {
        val wrapper = getItem(position)
        val builder = childrenBuilders.getOrNull(wrapper.builderIndex) ?: error("Can't find builder for index: ${wrapper.builderIndex}")
        return builder.dataSource.getDataItemId(wrapper.data, wrapper.positionInDataSource)
    }

    override fun getItemViewType(position: Int): Int {
        val wrapper = getItem(position)
        val builderIndex = wrapper.builderIndex
        if (builderIndex > MAX_BUILDER_INDEX) {
            error("BuilderIndex($builderIndex) great than MaxBuilderIndex($MAX_BUILDER_INDEX)")
        }
        val builder = childrenBuilders.getOrNull(builderIndex) ?: error("Can't find builder for index: $builderIndex")

        val builderItemViewType = builder.itemViewCreator.getItemViewType(
            positionInDataSource = wrapper.positionInDataSource,
            data = wrapper.data
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
            val wrapper = getItem(position)
            val builder = childrenBuilders.getOrNull(wrapper.builderIndex) ?: error("Can't find builder for index: ${wrapper.builderIndex}")
            builder.dataBinder.bindPayloadData(
                data = wrapper.data,
                view = holder.itemView,
                positionInDataSource = wrapper.positionInDataSource,
                payloads = payloads
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val wrapper = getItem(position)
        val builder = childrenBuilders.getOrNull(wrapper.builderIndex) ?: error("Can't find builder for index: ${wrapper.builderIndex}")
        builder.dataBinder.bindData(
            data = wrapper.data,
            view = holder.itemView,
            positionInDataSource = wrapper.positionInDataSource
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
        val combinedList = mutableListOf<CombinedItemWrapper>()
        var builderIndex = 0
        for (c in childrenBuilders) {
            val list = if (c.dataSource === child) {
                data
            } else {
                c.dataSource.lastRequestSubmitDataList ?: emptyList()
            }
            var i = 0
            for (item in list) {
                combinedList.add(CombinedItemWrapper(
                    data = item,
                    builderIndex = builderIndex,
                    positionInDataSource = i
                ))
                i++
            }
            builderIndex++
        }
        childrenRequestSubmitDataTasks.add(ChildRequestSubmitDataTask(child, callback))
        tUiUtilsLog.d(TAG, "Request submit list count: ${combinedList.size}")
        submitList(combinedList) {
            // Work on main thread.
            tUiUtilsLog.d(TAG, "Submitted list count: ${combinedList.size}")
            if (Looper.myLooper() == Looper.getMainLooper()) {
                ifHasWaitingRequestUpdateChildrenSubmitTheme()
            } else {
                Handler(Looper.getMainLooper()).post { ifHasWaitingRequestUpdateChildrenSubmitTheme() }
            }
        }
    }

    private fun ifHasWaitingRequestUpdateChildrenSubmitTheme() {
        val i = childrenRequestSubmitDataTasks.iterator()
        while (i.hasNext()) {
            i.next().callback?.run()
            i.remove()
        }
    }

    companion object {

        internal data class CombinedItemWrapper(
            val data: Any,
            val builderIndex: Int,
            val positionInDataSource: Int
        )

        private data class ChildRequestSubmitDataTask(
            val child: DataSource<Any>,
            val callback: Runnable?
        )

        private const val BUILDER_ITEM_VIEW_MASK: Int = 0x00_00_FF_FF
        private const val BUILDER_INDEX_MASK: Int = 0xFF_FF_00_00.toInt()
        private const val BUILDER_INDEX_MASK_OFFSET_BIT = 16
        private const val MAX_BUILDER_INDEX = 65535
        private const val MAX_ITEM_VIEW_TYPE = 65535
        private const val TAG = "CombinedAdapterImpl"
    }
}