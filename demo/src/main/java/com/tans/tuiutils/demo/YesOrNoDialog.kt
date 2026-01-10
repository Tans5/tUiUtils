package com.tans.tuiutils.demo

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.tans.tuiutils.view.clicks
import com.tans.tuiutils.demo.databinding.DialogYesOrNoBinding
import com.tans.tuiutils.dialog.BaseContinuationStateForceResultDialogFragment
import com.tans.tuiutils.dialog.createDefaultDialog

class YesOrNoDialog : BaseContinuationStateForceResultDialogFragment<Unit, Boolean>(Unit) {

    override val layoutId: Int = R.layout.dialog_yes_or_no
    override fun firstLaunchInitData(savedInstanceState: Bundle?) {

    }

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

        viewBinding.yesBt.clicks(lifecycleScope) {
            onResult(true)
        }

        viewBinding.noBt.clicks(lifecycleScope) {
            onResult(false)
        }
    }
}