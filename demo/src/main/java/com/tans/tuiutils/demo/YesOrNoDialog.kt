package com.tans.tuiutils.demo

import android.view.View
import com.tans.tuiutils.view.clicks
import com.tans.tuiutils.demo.databinding.DialogYesOrNoBinding
import com.tans.tuiutils.dialog.BaseSimpleCoroutineResultForceDialogFragment

class YesOrNoDialog : BaseSimpleCoroutineResultForceDialogFragment<Unit, Boolean>(Unit) {

    override val layoutId: Int = R.layout.dialog_yes_or_no

    override fun firstLaunchInitData() { }

    override fun bindContentView(view: View) {
        val viewBinding = DialogYesOrNoBinding.bind(view)

        viewBinding.yesBt.clicks(this) {
            onResult(true)
        }

        viewBinding.noBt.clicks(this) {
            onResult(false)
        }
    }
}