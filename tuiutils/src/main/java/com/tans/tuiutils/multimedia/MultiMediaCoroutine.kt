package com.tans.tuiutils.multimedia

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@MainThread
suspend fun ComponentActivity.pickVisualMediaSuspend(type: ActivityResultContracts.PickVisualMedia.VisualMediaType): Uri? {
    return suspendCancellableCoroutine { cont ->
        pickVisualMedia(type) { uri ->
            if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                cont.resume(uri)
            }
        }
    }
}

@MainThread
suspend fun ComponentActivity.pickImageSuspend(): Uri? {
    return pickVisualMediaSuspend(ActivityResultContracts.PickVisualMedia.ImageOnly)
}

@MainThread
suspend fun ComponentActivity.pickVideoSuspend(): Uri? {
    return pickVisualMediaSuspend(ActivityResultContracts.PickVisualMedia.VideoOnly)
}

@MainThread
suspend fun ComponentActivity.takeAPhotoSuspend(outputUri: Uri): Boolean {
    return suspendCancellableCoroutine { cont ->
        this.takeAPhoto(outputUri) {
            if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                cont.resume(it)
            }
        }
    }
}