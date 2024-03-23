package com.tans.tuiutils.demo

import android.app.Application
import com.tans.tuiutils.systembar.AutoApplySystemBarAnnotation

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AutoApplySystemBarAnnotation.init(this)
    }
}