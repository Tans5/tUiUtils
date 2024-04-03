package com.tans.tuiutils.adapter.impl.datasources

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.adapter.DataSource

open class DataSourceImpl<Data : Any>(
    private val areDataItemsTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    private val areDataItemsContentTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    private val getDataItemsChangePayloadParam: (d1: Data, d2: Data) -> Any? = { _, _ ->  null }
) : DataSource<Data> {

    final override var lastSubmittedDataList: List<Data>? = null

    final override var submittingDataList: List<Data>? = null

    final override fun areDataItemsTheSame(d1: Data, d2: Data): Boolean = areDataItemsTheSameParam(d1, d2)

    final override fun areDataItemsContentTheSame(d1: Data, d2: Data): Boolean = areDataItemsContentTheSameParam(d1, d2)

    override fun getDataItemsChangePayload(d1: Data, d2: Data): Any? = getDataItemsChangePayloadParam(d1, d2)

    override fun getLastSubmittedData(positionInDataSource: Int): Data? {
        return lastSubmittedDataList?.getOrNull(positionInDataSource)
    }

    override var attachedBuilder: AdapterBuilder<Data>? = null

    override var attachedRecyclerView: RecyclerView? = null
}