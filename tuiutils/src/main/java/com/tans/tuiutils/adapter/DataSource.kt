package com.tans.tuiutils.adapter

import androidx.annotation.MainThread

interface DataSource<Data : Any> : AdapterBuilderLife<Data> {

    var lastSubmittedDataList: List<Data>?

    var submittingDataList: List<Data>?

    @MainThread
    fun submitDataList(data: List<Data>, callback: Runnable? = null) {
        val builder = attachedBuilder
        if (builder == null) {
            error("Attached builder is null.")
        } else {
            submittingDataList = data
            builder.requestSubmitDataList(this, data) {
                lastSubmittedDataList = data
                submittingDataList = null
                callback?.run()
            }
        }
    }

    fun areDataItemsTheSame(d1: Data, d2: Data): Boolean

    fun areDataItemsContentTheSame(d1: Data, d2: Data): Boolean

    fun getDataItemsChangePayload(d1: Data, d2: Data): Any?

    fun getLastSubmittedData(positionInDataSource: Int): Data?

    fun getLastSubmittedDataSize(): Int = lastSubmittedDataList?.size ?: 0

    fun tryGetDataClass(): Class<Data>? = submittingDataList?.getOrNull(0)?.javaClass ?: lastSubmittedDataList?.getOrNull(0)?.javaClass
}