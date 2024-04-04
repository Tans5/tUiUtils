package com.tans.tuiutils.adapter

import androidx.annotation.MainThread

interface DataSource<Data : Any> : AdapterBuilderLife<Data> {

    var lastSubmittedDataList: List<Data>?


    @MainThread
    fun submitDataList(data: List<Data>, callback: Runnable? = null) {
        val builder = attachedBuilder
        if (builder == null) {
            error("Attached builder is null.")
        } else {
            lastSubmittedDataList = data
            builder.requestSubmitDataList(this, data, callback)
        }
    }

    fun areDataItemsTheSame(d1: Data, d2: Data): Boolean

    fun areDataItemsContentTheSame(d1: Data, d2: Data): Boolean

    fun getDataItemsChangePayload(d1: Data, d2: Data): Any?

    fun getDataItemId(data: Data, positionInDataSource: Int): Long

    fun getLastSubmittedData(positionInDataSource: Int): Data?

    fun getLastSubmittedDataSize(): Int = lastSubmittedDataList?.size ?: 0

    fun tryGetDataClass(): Class<Data>? = lastSubmittedDataList?.getOrNull(0)?.javaClass
}