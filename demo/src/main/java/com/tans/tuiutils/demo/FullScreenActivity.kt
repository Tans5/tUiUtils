package com.tans.tuiutils.demo

import android.os.Bundle
import android.view.View
import com.tans.tuiutils.activity.BaseActivity
import com.tans.tuiutils.systembar.annotation.FullScreenStyle

@FullScreenStyle
class FullScreenActivity : BaseActivity() {

    override val layoutId: Int = R.layout.activity_fit_system_window

    override fun firstLaunchInitData(savedInstanceState: Bundle?) {
    }

    override fun bindContentView(contentView: View) {
    }
}