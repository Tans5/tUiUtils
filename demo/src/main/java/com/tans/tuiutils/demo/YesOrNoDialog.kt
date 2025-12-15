package com.tans.tuiutils.demo

import android.app.Dialog
import android.view.View
import com.tans.tuiutils.view.clicks
import com.tans.tuiutils.demo.databinding.DialogYesOrNoBinding
import com.tans.tuiutils.dialog.BaseSimpleCoroutineResultForceDialogFragment
import com.tans.tuiutils.dialog.createDefaultDialog

class YesOrNoDialog : BaseSimpleCoroutineResultForceDialogFragment<Unit, Boolean>(Unit) {

    override val layoutId: Int = R.layout.dialog_yes_or_no

    override fun firstLaunchInitData() { }

    override fun createDialog(contentView: View): Dialog {
        isCancelable = false
        return requireActivity().createDefaultDialog(
            contentView = contentView,
            isCancelable = false,
            dimAmount = 0.0f,
            windowAnima = com.tans.tuiutils.R.style.tUiDefaultTopDialogAnima
        )
    }

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