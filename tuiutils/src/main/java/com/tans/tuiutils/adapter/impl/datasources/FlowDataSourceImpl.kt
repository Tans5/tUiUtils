package com.tans.tuiutils.adapter.impl.datasources

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import com.tans.tuiutils.Internal
import kotlinx.coroutines.flow.debounce

class FlowDataSourceImpl<Data : Any>(
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
    @Internal
    private var coroutineScope: CoroutineScope? = null

    @Internal
    override fun onAttachToBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        super.onAttachToBuilder(recyclerView, builder)
        val newCoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
        newCoroutineScope.launch(Dispatchers.Main) {
            dataFlow
                .debounce(20)
                .distinctUntilChanged()
                .flowOn(Dispatchers.IO)
                .collect {
                    submitDataList(it)
                }
        }
        coroutineScope = newCoroutineScope
    }

    @Internal
    override fun onDetachedFromBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        super.onDetachedFromBuilder(recyclerView, builder)
        coroutineScope?.cancel("onAttachToBuilder")
        coroutineScope = null
    }
}