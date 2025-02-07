package com.tans.tuiutils.adapter

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import com.tans.tuiutils.Internal
import com.tans.tuiutils.tUiUtilsLog

@Internal
interface DataSource<Data : Any> : AdapterBuilderLife<Data> {

    var lastRequestSubmitDataList: List<Data>?

    var lastSubmittedDataList: List<Data>?

    var dataClass: Class<Data>?

    val requestSubmitDataListCallbacks: MutableList<Runnable>

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
            val c = Runnable {
                lastSubmittedDataList = data
                callback?.run()
            }
            requestSubmitDataListCallbacks.add(c)
            builder.requestSubmitDataList(this, data) {
                if (requestSubmitDataListCallbacks.size > 1) {
                    tUiUtilsLog.w(DATASOURCE_TAG, "RequestSubmitDataListCallbacksCount: ${requestSubmitDataListCallbacks.size}")
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    ifHasWaitingCallbacksSubmitTheme()
                } else {
                    Handler(Looper.getMainLooper()).post { ifHasWaitingCallbacksSubmitTheme() }
                }
            }
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
    fun ifHasWaitingCallbacksSubmitTheme() {
        val i = requestSubmitDataListCallbacks.iterator()
        while (i.hasNext()) {
            i.next().run()
            i.remove()
        }
    }
}

private const val DATASOURCE_TAG = "DataSource"