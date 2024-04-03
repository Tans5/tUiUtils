package com.tans.tuiutils.dialog

interface DialogCancelableResultCallback<T : Any> {
    fun onResult(t: T)
    fun onCancel()

}