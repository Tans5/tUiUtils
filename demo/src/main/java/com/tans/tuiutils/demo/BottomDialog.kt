package com.tans.tuiutils.demo

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tans.tuiutils.demo.databinding.DialogBottomBinding
import com.tans.tuiutils.dialog.BaseDialogFragment
import com.tans.tuiutils.dialog.createBottomSheetDialog
import com.tans.tuiutils.dialog.createDefaultDialog

class BottomDialog : BaseDialogFragment() {

    override val contentViewHeightInScreenRatio: Float = 0.7f

    override fun createContentView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.dialog_bottom, parent, false)
    }

    override fun onBindContentView(view: View) {
        super.onBindContentView(view)
        val viewBinding = DialogBottomBinding.bind(view)
        // Do Logic.
    }

    override fun createDialog(contentView: View): Dialog {
        return requireActivity().createBottomSheetDialog(contentView = contentView) { behavior ->

        }
    }
}