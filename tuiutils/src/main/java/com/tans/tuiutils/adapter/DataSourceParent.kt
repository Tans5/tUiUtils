package com.tans.tuiutils.adapter

import androidx.annotation.MainThread
import com.tans.tuiutils.assertMainThread
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
interface DataSourceParent<Data : Any> {

    @MainThread
    fun requestSubmitDataList(child: DataSource<Data>, data: List<Data>, callback: Runnable?) {
        assertMainThread { "DataSourceParent#requestSubmitDataList() need work on main thread." }
    }

}