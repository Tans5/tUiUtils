package com.tans.tuiutils.multimedia

import android.net.Uri
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

fun FragmentActivity.pickVisualMediaRx(mimeType: String): Maybe<Uri> {
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

fun Fragment.pickVisualMediaRx(mimeType: String): Maybe<Uri> {
    return activity?.pickVisualMediaRx(mimeType) ?: Maybe.error(Throwable("Fragment's parent activity is null."))
}

fun FragmentActivity.pickImageRx(): Maybe<Uri> {
    return pickVisualMediaRx(mimeType = "image/*")
}

fun Fragment.pickImageRx(): Maybe<Uri> {
    return activity?.pickImageRx() ?: Maybe.error(Throwable("Fragment's parent activity is null."))
}

fun FragmentActivity.pickVideoRx(): Maybe<Uri> {
    return pickVisualMediaRx(mimeType = "video/*")
}

fun Fragment.pickVideoRx(): Maybe<Uri> {
    return activity?.pickVideoRx() ?: Maybe.error(Throwable("Fragment's parent activity is null."))
}

fun FragmentActivity.takeAPhotoRx(outputUri: Uri): Single<Boolean> {
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

fun Fragment.takeAPhotoRx(outputUri: Uri): Single<Boolean> {
    return activity?.takeAPhotoRx(outputUri) ?: Single.error(Throwable("Fragment's parent activity is null."))
}