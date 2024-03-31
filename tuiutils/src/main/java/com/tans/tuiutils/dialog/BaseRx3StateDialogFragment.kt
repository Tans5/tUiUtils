package com.tans.tuiutils.dialog

import com.tans.tuiutils.state.Rx3Life
import com.tans.tuiutils.state.Rx3State

abstract class BaseRx3StateDialogFragment<State: Any>(defaultState: State)
    : BaseDialogFragment(), Rx3State<State> by Rx3State(defaultState), Rx3Life by Rx3Life() {

    override fun onDestroy() {
        super.onDestroy()
        lifeCompositeDisposable.clear()
    }
}