package com.tans.tuiutils.fragment

import android.view.View
import com.tans.tuiutils.state.Rx3Life
import com.tans.tuiutils.state.Rx3State

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseRx3StateFragment<State : Any>(defaultState: State) : BaseFragment(), Rx3State<State> by Rx3State(defaultState) {

    protected var uiRxLife: Rx3Life? = null
        private set

    protected var dataRxLife: Rx3Life? = null
        private set

    abstract fun Rx3Life.firstLaunchInitDataRx()

    final override fun firstLaunchInitData() {
        dataRxLife?.lifeCompositeDisposable?.clear()
        val newDataRxLife = Rx3Life()
        newDataRxLife.firstLaunchInitDataRx()
        dataRxLife = newDataRxLife
    }

    abstract fun Rx3Life.bindContentViewCoroutine(contentView: View)

    final override fun bindContentView(contentView: View) {
        uiRxLife?.lifeCompositeDisposable?.clear()
        val newRxUiLife = Rx3Life()
        newRxUiLife.bindContentViewCoroutine(contentView)
        uiRxLife = newRxUiLife
    }

    override fun onDestroy() {
        super.onDestroy()
        uiRxLife?.lifeCompositeDisposable?.clear()
        dataRxLife?.lifeCompositeDisposable?.clear()
    }
}