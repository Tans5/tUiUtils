package com.tans.tuiutils

import android.os.Looper

internal inline fun assert(assert: Boolean, errorMsg: () -> String) {
    if (!assert) {
        error(errorMsg())
    }
}

internal inline fun assertMainThread(errorMsg: () -> String) {
    assert(Looper.getMainLooper() === Looper.myLooper(), errorMsg)
}