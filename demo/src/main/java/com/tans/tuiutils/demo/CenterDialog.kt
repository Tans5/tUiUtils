package com.tans.tuiutils.demo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tans.tuiutils.demo.databinding.DialogCenterBinding
import com.tans.tuiutils.dialog.BaseDialogFragment

class CenterDialog : BaseDialogFragment() {

    override fun createContentView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.dialog_center, parent, false)
    }

    override fun onDialogCreated(view: View) {
        super.onDialogCreated(view)
        val viewBinding = DialogCenterBinding.bind(view)
        // Do some logic.
    }
}