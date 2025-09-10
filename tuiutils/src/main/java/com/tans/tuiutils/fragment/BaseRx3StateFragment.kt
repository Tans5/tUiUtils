package com.tans.tuiutils.fragment

import android.view.View
import com.tans.tuiutils.state.Rx3Life
import com.tans.tuiutils.state.Rx3State

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseRx3StateFragment<State : Any>(protected val defaultState: State) : BaseFragment(), Rx3State<State> by Rx3State(defaultState) {

    open val firstLaunchCheckDefaultState: Boolean = true

    protected val dataRxLife: Rx3Life by lazyViewModelField("dataRxLife") {
        Rx3Life()
    }

    protected var uiRxLife: Rx3Life? = null
        private set

    abstract fun Rx3Life.firstLaunchInitDataRx()

    final override fun firstLaunchInitData() {
        if (firstLaunchCheckDefaultState) {
            if (defaultState == bindState().firstOrError().blockingGet()) {
                dataRxLife.firstLaunchInitDataRx()
            }
        } else {
            dataRxLife.firstLaunchInitDataRx()
        }
    }

    abstract fun Rx3Life.bindContentViewRx(contentView: View)

    final override fun bindContentView(contentView: View, useLastContentView: Boolean) {
        uiRxLife?.lifeCompositeDisposable?.clear()
        val newRxUiLife = Rx3Life()
        newRxUiLife.bindContentViewRx(contentView)
        uiRxLife = newRxUiLife
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiRxLife?.lifeCompositeDisposable?.clear()
        uiRxLife = null
    }

    override fun onViewModelCleared() {
        super.onViewModelCleared()
        dataRxLife.lifeCompositeDisposable.clear()
    }
}