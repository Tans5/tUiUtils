package com.tans.tuiutils.permission

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @return first: granted permissions, second: not granted permissions.
 */
@MainThread
suspend fun FragmentActivity.permissionsRequestSuspend(vararg permissions: String): Pair<Set<String>, Set<String>> {
    return suspendCancellableCoroutine { cont ->
        this.permissionsRequest(
            permissions = permissions,
            error = {
                if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                    cont.resumeWithException(Throwable(it))
                }
            },
            callback = { granted, notGranted ->
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
suspend fun FragmentActivity.permissionsRequestSimplifySuspend(vararg permissions: String): Boolean {
    return suspendCancellableCoroutine { cont ->
        this.permissionsRequestSimplify(
            permissions = permissions,
            error = {
                if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                    cont.resumeWithException(Throwable(it))
                }
            },
            callback = {
                if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                    cont.resume(it)
                }
            })
    }
}

@MainThread
suspend fun Fragment.permissionsRequestSimplifySuspend(vararg permissions: String): Boolean {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.permissionsRequestSimplifySuspend(*permissions)
}