package com.tans.tuiutils.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tans.tuiutils.systembar.annotation.SystemBarStyle

@SystemBarStyle
class TransparentSystemBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent_system_bar)
    }
}