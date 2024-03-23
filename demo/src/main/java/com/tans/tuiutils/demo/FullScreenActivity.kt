package com.tans.tuiutils.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tans.tuiutils.systembar.annotation.FullScreenStyle

@FullScreenStyle
class FullScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fit_system_window)
    }
}