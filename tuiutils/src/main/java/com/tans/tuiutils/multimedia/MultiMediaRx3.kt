package com.tans.tuiutils.multimedia

import android.net.Uri
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

fun FragmentActivity.pickVisualMediaRx3(mimeType: String): Maybe<Uri> {
    return Maybe.create { emit ->
        val r = Runnable {
            pickVisualMedia(
                mimeType = mimeType,
                error = {
                    if (!emit.isDisposed) {
                        emit.onError(Throwable(it))
                    }
                },
                callback = { uri ->
                    if (!emit.isDisposed) {
                        if (uri != null) {
                            emit.onSuccess(uri)
                        } else {
                            emit.onComplete()
                        }
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

fun Fragment.pickVisualMediaRx3(mimeType: String): Maybe<Uri> {
    return activity?.pickVisualMediaRx3(mimeType) ?: Maybe.error(Throwable("Fragment's parent activity is null."))
}

fun FragmentActivity.pickImageRx3(): Maybe<Uri> {
    return pickVisualMediaRx3(mimeType = "image/*")
}

fun Fragment.pickImageRx3(): Maybe<Uri> {
    return activity?.pickImageRx3() ?: Maybe.error(Throwable("Fragment's parent activity is null."))
}

fun FragmentActivity.pickVideoRx3(): Maybe<Uri> {
    return pickVisualMediaRx3(mimeType = "video/*")
}

fun Fragment.pickVideoRx3(): Maybe<Uri> {
    return activity?.pickVideoRx3() ?: Maybe.error(Throwable("Fragment's parent activity is null."))
}

fun FragmentActivity.takeAPhotoRx3(outputUri: Uri): Single<Boolean> {
    return Single.create { emit ->
        val r = Runnable {
            takeAPhoto(
                outputFileUri = outputUri,
                error = {
                    if (!emit.isDisposed) {
                        emit.onError(Throwable(it))
                    }
                },
                callback = {
                    if (!emit.isDisposed) {
                        emit.onSuccess(it)
                    }
                })
        }
        if (Looper.getMainLooper() === Looper.myLooper()) {
            r.run()
        } else {
            runOnUiThread(r)
        }
    }
}

fun Fragment.takeAPhotoRx3(outputUri: Uri): Single<Boolean> {
    return activity?.takeAPhotoRx3(outputUri) ?: Single.error(Throwable("Fragment's parent activity is null."))
}