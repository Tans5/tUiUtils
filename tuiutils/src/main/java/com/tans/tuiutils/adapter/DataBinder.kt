package com.tans.tuiutils.adapter

import android.view.View

interface DataBinder<Data : Any> : AdapterBuilderLife<Data> {

    fun bindData(data: Data, view: View, position: Int)

    fun bindPayloadData(data: Data, view: View, position: Int)
}