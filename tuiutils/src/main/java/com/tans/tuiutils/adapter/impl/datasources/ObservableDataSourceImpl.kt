package com.tans.tuiutils.adapter.impl.datasources

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder
import com.tans.tuiutils.state.Rx3Life
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class ObservableDataSourceImpl<Data : Any>(
    private val dataObservable: Observable<List<Data>>,
    areDataItemsTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    areDataItemsContentTheSameParam: (d1: Data, d2: Data) -> Boolean = { d1, d2 -> d1 == d2 },
    getDataItemsChangePayloadParam: (d1: Data, d2: Data) -> Any? = { _, _ -> null },
    getDataItemIdParam: (data: Data, positionInDataSource: Int) -> Long = { _, _ -> RecyclerView.NO_ID }
) : DataSourceImpl<Data>(
    areDataItemsTheSameParam = areDataItemsTheSameParam,
    areDataItemsContentTheSameParam = areDataItemsContentTheSameParam,
    getDataItemsChangePayloadParam = getDataItemsChangePayloadParam,
    getDataItemIdParam = getDataItemIdParam
) {
    private var rx3Life: Rx3Life? = null

    override fun onAttachToBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        super.onAttachToBuilder(recyclerView, builder)
        val newRx3Life = Rx3Life()
        with(newRx3Life) {
            dataObservable
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    submitDataList(it)
                }
                .bindLife()
        }
        rx3Life = newRx3Life
    }

    override fun onDetachedFromBuilder(recyclerView: RecyclerView, builder: AdapterBuilder<Data>) {
        super.onDetachedFromBuilder(recyclerView, builder)
        rx3Life?.lifeCompositeDisposable?.clear()
        rx3Life = null
    }
}