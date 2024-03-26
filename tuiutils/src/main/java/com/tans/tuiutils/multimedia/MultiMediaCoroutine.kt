package com.tans.tuiutils.multimedia

import android.net.Uri
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@MainThread
suspend fun FragmentActivity.pickVisualMediaSuspend(mimeType: String): Uri? {
    return suspendCancellableCoroutine { cont ->
        pickVisualMedia(mimeType) { uri ->
            if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                cont.resume(uri)
            }
        }
    }
}

@MainThread
suspend fun FragmentActivity.pickImageSuspend(): Uri? {
    return pickVisualMediaSuspend(mimeType = "image/*")
}

@MainThread
suspend fun FragmentActivity.pickVideoSuspend(): Uri? {
    return pickVisualMediaSuspend(mimeType = "video/*")
}

@MainThread
suspend fun FragmentActivity.takeAPhotoSuspend(outputUri: Uri): Boolean {
    return suspendCancellableCoroutine { cont ->
        this.takeAPhoto(outputUri) {
            if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                cont.resume(it)
            }
        }
    }
}