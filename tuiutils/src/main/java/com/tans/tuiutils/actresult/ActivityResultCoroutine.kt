package com.tans.tuiutils.actresult

import android.content.Intent
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

@MainThread
suspend fun FragmentActivity.startActivityResultSuspend(targetActivityIntent: Intent): Pair<Int, Intent?> {
    return suspendCancellableCoroutine { cont ->
        startActivityResult(
            targetActivityIntent = targetActivityIntent,
            error = {
                if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                    cont.resumeWithException(Throwable(it))
                }
            },
            callback = { resultCode, resultData ->
                if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                    cont.resume(resultCode to resultData)
                }
            }
        )
    }
}

@MainThread
suspend fun Fragment.startActivityResultSuspend(targetActivityIntent: Intent): Pair<Int, Intent?> {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.startActivityResultSuspend(targetActivityIntent)
}