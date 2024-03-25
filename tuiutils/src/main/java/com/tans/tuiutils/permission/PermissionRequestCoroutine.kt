package com.tans.tuiutils.permission

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @return first: granted permissions, second: not granted permissions.
 */
@MainThread
suspend fun ComponentActivity.permissionsRequestSuspend(vararg permissions: String): Pair<Set<String>, Set<String>> {
    return suspendCancellableCoroutine { cont ->
        this.permissionsRequest(permissions = permissions, callback = { granted, notGranted ->
            if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                cont.resume(granted to notGranted)
            }
        })
    }
}

@MainThread
suspend fun Fragment.permissionsRequestSuspend(vararg permissions: String): Pair<Set<String>, Set<String>> {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.permissionsRequestSuspend(*permissions)
}

@MainThread
suspend fun ComponentActivity.permissionsRequestSimplifySuspend(vararg permissions: String): Boolean {
    return suspendCancellableCoroutine { cont ->
        this.permissionsRequestSimplify(permissions = permissions, callback = {
            if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                cont.resume(it)
            }
        })
    }
}