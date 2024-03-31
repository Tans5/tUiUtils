package com.tans.tuiutils.activity

import android.view.View
import com.tans.tuiutils.state.Rx3Life
import com.tans.tuiutils.state.Rx3State
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseRx3StateActivity<State : Any>(defaultState: State) :
    BaseViewModelFieldActivity(), Rx3State<State> {


    override val stateSubject: Subject<State> by lazyViewModelField("stateSubject") {
        BehaviorSubject.createDefault(defaultState).toSerialized()
    }

    protected val uiRxLife: Rx3Life by lazy {
        object : Rx3Life {
            override val lifeCompositeDisposable: CompositeDisposable = CompositeDisposable()
        }
    }

    protected val dataRxLife: Rx3Life by lazyViewModelField("dataRxLife") {
        object : Rx3Life {
            override val lifeCompositeDisposable: CompositeDisposable = CompositeDisposable()
        }
    }

    abstract fun Rx3Life.firstLaunchInitDataRx()

    final override fun firstLaunchInitData() {
        dataRxLife.firstLaunchInitDataRx()
    }

    abstract fun Rx3Life.bindContentViewCoroutine(contentView: View)

    final override fun bindContentView(contentView: View) {
        uiRxLife.bindContentViewCoroutine(contentView)
    }

    override fun onDestroy() {
        super.onDestroy()
        uiRxLife.lifeCompositeDisposable.clear()
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        dataRxLife.lifeCompositeDisposable.clear()
    }

}