package com.tans.tuiutils.adapter

interface DataSourceParent<Data : Any> {

    fun requestSubmitDataList(child: DataSource<Data>, data: List<Data>, callback: Runnable?)

}