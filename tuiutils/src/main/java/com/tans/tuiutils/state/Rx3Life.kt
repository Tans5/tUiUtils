package com.tans.tuiutils.state

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable

interface Rx3Life {
    val lifeCompositeDisposable: CompositeDisposable

    fun <T : Any> Observable<T>.bindLife() {
        lifeCompositeDisposable.add(this.subscribe({}, {}, {}))
    }

    fun Completable.bindLife() {
        lifeCompositeDisposable.add(this.subscribe({ }, {}))
    }

    fun <T : Any> Single<T>.bindLife() {
        lifeCompositeDisposable.add(this.subscribe({ }, { }))
    }

    fun <T : Any> Maybe<T>.bindLife() {
        lifeCompositeDisposable.add(this.subscribe ({ }, { }, { }))
    }
}

fun Rx3Life(): Rx3Life = object : Rx3Life {
    override val lifeCompositeDisposable: CompositeDisposable = CompositeDisposable()
}