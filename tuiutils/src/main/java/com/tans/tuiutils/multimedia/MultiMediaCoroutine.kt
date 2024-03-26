package com.tans.tuiutils.multimedia

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@MainThread
suspend fun FragmentActivity.pickVisualMediaSuspend(type: ActivityResultContracts.PickVisualMedia.VisualMediaType): Uri? {
    return suspendCancellableCoroutine { cont ->
        pickVisualMedia(type) { uri ->
            if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                cont.resume(uri)
            }
        }
    }
}

@MainThread
suspend fun FragmentActivity.pickImageSuspend(): Uri? {
    return pickVisualMediaSuspend(ActivityResultContracts.PickVisualMedia.ImageOnly)
}

@MainThread
suspend fun FragmentActivity.pickVideoSuspend(): Uri? {
    return pickVisualMediaSuspend(ActivityResultContracts.PickVisualMedia.VideoOnly)
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