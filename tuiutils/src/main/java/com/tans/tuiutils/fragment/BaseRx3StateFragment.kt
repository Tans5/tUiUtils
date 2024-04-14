package com.tans.tuiutils.fragment

import android.view.View
import com.tans.tuiutils.state.Rx3Life
import com.tans.tuiutils.state.Rx3State

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseRx3StateFragment<State : Any>(protected val defaultState: State) : BaseFragment(), Rx3State<State> by Rx3State(defaultState) {

    open val firstLaunchCheckDefaultState: Boolean = true

    protected var uiRxLife: Rx3Life? = null
        private set

    protected var dataRxLife: Rx3Life? = null
        private set

    abstract fun Rx3Life.firstLaunchInitDataRx()

    final override fun firstLaunchInitData() {
        dataRxLife?.lifeCompositeDisposable?.clear()
        val newDataRxLife = Rx3Life()
        dataRxLife = newDataRxLife
        if (firstLaunchCheckDefaultState) {
            if (defaultState == bindState().firstOrError().blockingGet()) {
                newDataRxLife.firstLaunchInitDataRx()
            }
        } else {
            newDataRxLife.firstLaunchInitDataRx()
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

    override fun onDestroy() {
        super.onDestroy()
        dataRxLife?.lifeCompositeDisposable?.clear()
        dataRxLife = null
    }
}