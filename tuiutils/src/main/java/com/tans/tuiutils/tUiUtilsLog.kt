package com.tans.tuiutils

import android.util.Log

@Suppress("ClassName")
internal object tUiUtilsLog {

    fun d(tag: String = TAG, msg: String) {
        Log.d(tag, msg)
    }

    fun w(tag: String = TAG, msg: String, e: Throwable? = null) {
        Log.w(tag, msg, e)
    }

    fun e(tag: String = TAG, msg: String, e: Throwable? = null) {
        Log.e(tag, msg, e)
    }

    private const val TAG = "tUiUtils"
}