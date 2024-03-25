package com.tans.tuiutils.permission

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.core.Single

/**
 * @return first: granted permissions, second: not granted permissions.
 */
fun ComponentActivity.permissionsRequestRx3(vararg permissions: String): Single<Pair<Set<String>, Set<String>>> {
    return Single.create { emitter ->
        val r = Runnable {
            this.permissionsRequest(permissions = permissions) { granted: Set<String>, notGranted: Set<String> ->
                emitter.onSuccess(granted to notGranted)
            }
        }
        if (Looper.getMainLooper() === Looper.myLooper()) {
            r.run()
        } else {
            runOnUiThread(r)
        }
    }
}

/**
 * @return first: granted permissions, second: not granted permissions.
 */
fun Fragment.permissionsRequestRx3(vararg permissions: String): Single<Pair<Set<String>, Set<String>>> {
    val act = activity
    return act?.permissionsRequestRx3(*permissions)
        ?: Single.error(Throwable("Activity is null, can't request permissions."))
}

fun ComponentActivity.permissionsRequestSimplifyRx3(vararg permissions: String): Single<Boolean> {
    return Single.create { emitter ->
        val r = Runnable {
            this.permissionsRequestSimplify(permissions = permissions) {
                emitter.onSuccess(it)
            }
        }
        if (Looper.getMainLooper() === Looper.myLooper()) {
            r.run()
        } else {
            runOnUiThread(r)
        }
    }
}

fun Fragment.permissionRequestSimplifyRx3(vararg permissions: String): Single<Boolean> {
    val act = activity
    return act?.permissionsRequestSimplifyRx3(*permissions)
        ?: Single.error(Throwable("Activity is null, can't request permissions."))
}