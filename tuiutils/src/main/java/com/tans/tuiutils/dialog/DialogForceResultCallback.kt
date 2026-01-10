package com.tans.tuiutils.dialog

interface DialogForceResultCallback<T : Any> {
    fun onResult(t: T)

    fun onError(e: String)
}