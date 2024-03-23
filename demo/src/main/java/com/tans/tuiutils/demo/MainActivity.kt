package com.tans.tuiutils.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tans.tuiutils.demo.databinding.ActivityMainBinding
import com.tans.tuiutils.systembar.annotation.FitSystemWindow
import com.tans.tuiutils.systembar.annotation.SystemBarStyle

@SystemBarStyle(
    lightStatusBar = true,
    lightNavigationBar = true
)
@FitSystemWindow
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.transparentSystemBarActBt.setOnClickListener {
            startActivity(Intent(this, TransparentSystemBarActivity::class.java))
        }

        viewBinding.fitSystemWindowActBt.setOnClickListener {
            startActivity(Intent(this, FitSystemWindowActivity::class.java))
        }

        viewBinding.fullScreenActBt.setOnClickListener {
            startActivity(Intent(this, FullScreenActivity::class.java))
        }

        viewBinding.centerDialogBt.setOnClickListener {
            CenterDialog().show(supportFragmentManager, "CenterDialog${System.currentTimeMillis()}")
        }

        viewBinding.bottomDialogBt.setOnClickListener {
            BottomDialog().show(supportFragmentManager, "BottomDialog${System.currentTimeMillis()}")
        }
    }
}