package com.tans.tuiutils.state

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

interface Rx3State<State : Any> {
    val stateSubject: Subject<State>

    fun bindState(): Observable<State> = stateSubject

    fun updateState(newState: (State) -> State): Single<State> = stateSubject.firstOrError()
        .map(newState)
        .doOnSuccess { state: State -> stateSubject.onNext(state) }

    fun updateStateCompletable(newState: (State) -> State): Completable = updateState(newState).ignoreElement()

    fun <T : Any> render(mapper: ((State) -> T), handler: (T) -> Unit): Completable = bindState()
        .map(mapper)
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { t ->
            handler(t)
        }
        .ignoreElements()

    fun render(handler: (State) -> Unit): Completable = bindState()
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { t ->
            handler(t)
        }
        .ignoreElements()
}

fun <State : Any> Rx3State(defaultState: State): Rx3State<State> {
    return object : Rx3State<State> {
        override val stateSubject: Subject<State> = BehaviorSubject.createDefault(defaultState).toSerialized()
    }
}