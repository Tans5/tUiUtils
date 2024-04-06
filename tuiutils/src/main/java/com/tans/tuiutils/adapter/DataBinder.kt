package com.tans.tuiutils.adapter

import android.view.View
import com.tans.tuiutils.Internal

@Internal
interface DataBinder<Data : Any> : AdapterBuilderLife<Data> {

    val payloadDataBinders: MutableMap<Any, ((data: Data, view: View, positionInDataSource: Int) -> Unit)?>

    fun bindData(data: Data, view: View, positionInDataSource: Int)

    fun bindPayloadData(data: Data, view: View, positionInDataSource: Int, payloads: List<Any>)

    fun addPayloadDataBinder(payload: Any, binder: (data: Data, view: View, positionInDataSource: Int) -> Unit): DataBinder<Data> {
        payloadDataBinders[payload] = binder
        return this
    }

}