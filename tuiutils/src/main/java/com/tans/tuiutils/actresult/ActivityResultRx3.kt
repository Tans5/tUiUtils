package com.tans.tuiutils.actresult

import android.content.Intent
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.core.Single

fun FragmentActivity.startActivityResultRx3(targetActivityIntent: Intent): Single<Pair<Int, Intent?>> {
    return Single.create { emitter ->
        val r = Runnable {
            startActivityResult(
                targetActivityIntent = targetActivityIntent,
                error = {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(it))
                    }
                },
                callback = { resultCode: Int, resultData: Intent? ->
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(resultCode to resultData)
                    }
                }
            )
        }
        if (Looper.getMainLooper() === Looper.myLooper()) {
            r.run()
        } else {
            runOnUiThread(r)
        }
    }
}

fun Fragment.startActivityResultRx3(targetActivityIntent: Intent): Single<Pair<Int, Intent?>> {
    return activity?.startActivityResultRx3(targetActivityIntent) ?: Single.error(Throwable("Fragment's parent activity is null."))
}