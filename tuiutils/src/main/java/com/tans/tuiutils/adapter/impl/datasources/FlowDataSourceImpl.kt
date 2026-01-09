package com.tans.tuiutils.adapter.impl.datasources

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job


class FlowDataSourceImpl<Data : Any>(
    private val coroutineScope: CoroutineScope,
    private val dataFlow: Flow<List<Data>>,
    areDataItemsTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    areDataItemsContentTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    getDataItemsChangePayloadParam: (d1: Data, d2: Data) -> Any? = { _, _ -> null },
    getDataItemIdParam: (data: Data, positionInDataSource: Int) -> Long = { _, _ -> RecyclerView.NO_ID }
) : DataSourceImpl<Data>(
    areDataItemsTheSameParam = areDataItemsTheSameParam,
    areDataItemsContentTheSameParam = areDataItemsContentTheSameParam,
    getDataItemsChangePayloadParam = getDataItemsChangePayloadParam,
    getDataItemIdParam = getDataItemIdParam
) {

    private var lastJob: Job? = null

    override fun onAttachToBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        super.onAttachToBuilder(recyclerView, builder)
        lastJob?.cancel()
        lastJob = coroutineScope.launch(Dispatchers.Main.immediate) {
            dataFlow
                .distinctUntilChanged()
                // .flowOn(AppDispatchers.IO)
                .collect {
                    submitDataList(it)
                }
        }
    }

    override fun onDetachedFromBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        super.onDetachedFromBuilder(recyclerView, builder)
        lastJob?.cancel()
        lastJob = null
    }
}