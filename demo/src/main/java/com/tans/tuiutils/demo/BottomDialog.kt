package com.tans.tuiutils.demo

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tans.tuiutils.demo.databinding.DialogBottomBinding
import com.tans.tuiutils.dialog.BaseDialogFragment
import com.tans.tuiutils.systembar.lightSystemBar

class BottomDialog : BaseDialogFragment() {

    override val gravity: Int = Gravity.BOTTOM

    override val contentViewHeightInScreenRatio: Float = 0.7f

    override fun createContentView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.dialog_bottom, parent, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.lightSystemBar(lightStatusBar = true, lightNavigationBar = true)
        }
    }

    override fun onDialogCreated(view: View) {
        super.onDialogCreated(view)
        val viewBinding = DialogBottomBinding.bind(view)
        // Do Logic.
    }
}