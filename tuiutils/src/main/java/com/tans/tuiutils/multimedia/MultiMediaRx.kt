package com.tans.tuiutils.multimedia

import android.net.Uri
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

fun FragmentActivity.pickVisualMediaRx(type: ActivityResultContracts.PickVisualMedia.VisualMediaType): Maybe<Uri> {
    return Maybe.create { emit ->
        val r = Runnable {
            pickVisualMedia(type) { uri ->
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
    return pickVisualMediaRx(ActivityResultContracts.PickVisualMedia.ImageOnly)
}

fun FragmentActivity.pickVideoRx(): Maybe<Uri> {
    return pickVisualMediaRx(ActivityResultContracts.PickVisualMedia.VideoOnly)
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