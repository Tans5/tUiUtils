package com.tans.tuiutils.actresult

import android.content.Intent
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tans.tuiutils.assertMainThread

@MainThread
fun FragmentActivity.startActivityResult(
    targetActivityIntent: Intent,
    error: (msg: String) -> Unit,
    callback: (resultCode: Int, resultData: Intent?) -> Unit
) {
    assertMainThread { "startActivityResult() need invoke in main thread." }
    val fragment = ActivityResultFragment(
        targetActivityIntent = targetActivityIntent,
        error = error,
        callback = callback
    )
    val tc = supportFragmentManager.beginTransaction()
    tc.add(fragment, "ActivityResultFragment#${System.currentTimeMillis()}")
    tc.commitAllowingStateLoss()
}

@MainThread
fun Fragment.startActivityResult(
    targetActivityIntent: Intent,
    error: (msg: String) -> Unit,
    callback: (resultCode: Int, resultData: Intent?) -> Unit
) {
    val act = this.activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.startActivityResult(targetActivityIntent, error, callback)
}