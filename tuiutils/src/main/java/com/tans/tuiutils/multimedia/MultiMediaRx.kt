package com.tans.tuiutils.multimedia

import android.net.Uri
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

fun FragmentActivity.pickVisualMediaRx(mimeType: String): Maybe<Uri> {
    return Maybe.create { emit ->
        val r = Runnable {
            pickVisualMedia(mimeType) { uri ->
                if (uri != null) {
                    emit.onSuccess(uri)
                } else {
                    emit.onComplete()
                }
            }
        }
        if (Looper.getMainLooper() === Looper.myLooper()) {
            r.run()
        } else {
            runOnUiThread(r)
        }
    }
}

fun FragmentActivity.pickImageRx(): Maybe<Uri> {
    return pickVisualMediaRx(mimeType = "image/*")
}

fun FragmentActivity.pickVideoRx(): Maybe<Uri> {
    return pickVisualMediaRx(mimeType = "video/*")
}

fun FragmentActivity.takeAPhotoRx(outputUri: Uri): Single<Boolean> {
    return Single.create { emit ->
        val r = Runnable {
            takeAPhoto(outputUri) {
                emit.onSuccess(it)
            }
        }
        if (Looper.getMainLooper() === Looper.myLooper()) {
            r.run()
        } else {
            runOnUiThread(r)
        }
    }
}