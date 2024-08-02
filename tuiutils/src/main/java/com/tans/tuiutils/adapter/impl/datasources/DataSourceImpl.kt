package com.tans.tuiutils.adapter.impl.datasources

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataSource
import com.tans.tuiutils.Internal

open class DataSourceImpl<Data : Any>(
    private val areDataItemsTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    private val areDataItemsContentTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    private val getDataItemsChangePayloadParam: (d1: Data, d2: Data) -> Any? = { _, _ ->  null },
    private val getDataItemIdParam: (data: Data, positionInDataSource: Int) -> Long = { _, _ -> RecyclerView.NO_ID }
) : DataSource<Data> {

    @Internal
    final override var lastRequestSubmitDataListCallback: Runnable? = null

    @Internal
    final override var lastRequestSubmitDataList: List<Data>? = null

    @Internal
    final override var lastSubmittedDataList: List<Data>? = null

    @Internal
    override fun getDataItemId(data: Data, positionInDataSource: Int): Long = getDataItemIdParam(data, positionInDataSource)

    @Internal
    final override fun areDataItemsTheSame(d1: Data, d2: Data): Boolean = areDataItemsTheSameParam(d1, d2)

    @Internal
    final override fun areDataItemsContentTheSame(d1: Data, d2: Data): Boolean = areDataItemsContentTheSameParam(d1, d2)

    @Internal
    override fun getDataItemsChangePayload(d1: Data, d2: Data): Any? = getDataItemsChangePayloadParam(d1, d2)

    @Internal
    override var attachedBuilder: AdapterBuilder<Data>? = null

    @Internal
    override var attachedRecyclerView: RecyclerView? = null

    @Internal
    override var dataClass: Class<Data>? = null
}