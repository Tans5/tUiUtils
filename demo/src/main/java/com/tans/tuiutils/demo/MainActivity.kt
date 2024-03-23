package com.tans.tuiutils.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tans.tuiutils.systembar.annotation.FullScreenStyle
import com.tans.tuiutils.systembar.annotation.SystemBarStyle

// @FullScreenStyle
@SystemBarStyle(
    lightStatusBar = false,
    lightNavigationBar = false
)
// @FitSystemWindow
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}