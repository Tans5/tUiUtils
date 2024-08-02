package com.tans.tuiutils.adapter

import androidx.annotation.MainThread
import com.tans.tuiutils.Internal

@Internal
interface DataSource<Data : Any> : AdapterBuilderLife<Data> {

    var lastRequestSubmitDataListCallback: Runnable?

    var lastRequestSubmitDataList: List<Data>?

    var lastSubmittedDataList: List<Data>?

    var dataClass: Class<Data>?

    @MainThread
    fun submitDataList(data: List<Data>, callback: Runnable? = null) {
        val builder = attachedBuilder
        if (builder == null) {
            error("Attached builder is null.")
        } else {
            if (dataClass == null) {
                dataClass = data.getOrNull(0)?.javaClass
            }
            lastRequestSubmitDataList = data
            val c = kotlinx.coroutines.Runnable {
                lastSubmittedDataList = data
                callback?.run()
            }
            lastRequestSubmitDataListCallback = c
            builder.requestSubmitDataList(this, data, c)
        }
    }

    fun areDataItemsTheSame(d1: Data, d2: Data): Boolean

    fun areDataItemsContentTheSame(d1: Data, d2: Data): Boolean

    fun getDataItemsChangePayload(d1: Data, d2: Data): Any?

    fun getDataItemId(data: Data, positionInDataSource: Int): Long

    @MainThread
    fun getLastSubmittedData(positionInDataSource: Int): Data? {
        return lastSubmittedDataList?.getOrNull(positionInDataSource)
    }

    @MainThread
    fun getLastRequestSubmitData(positionInDataSource: Int): Data? {
        return lastRequestSubmitDataList?.getOrNull(positionInDataSource)
    }

    @MainThread
    fun getLastSubmittedDataSize(): Int = lastSubmittedDataList?.size ?: 0

    @MainThread
    fun getLastRequestSubmitDataSize(): Int = lastRequestSubmitDataList?.size ?: 0

    @MainThread
    fun tryGetDataClass(): Class<Data>? = dataClass

    @MainThread
    fun isWaitingSubmitCallback(): Boolean {
        val request = lastRequestSubmitDataList
        val submitted = lastSubmittedDataList
        return if (request != null) {
            request != submitted
        } else {
            false
        }
    }

    @MainThread
    fun ifWaitingSubmitIt() {
        if (isWaitingSubmitCallback()) {
            lastRequestSubmitDataListCallback?.run()
            lastRequestSubmitDataListCallback = null
        }
    }
}