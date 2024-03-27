package com.tans.tuiutils.multimedia

import android.net.Uri
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@MainThread
suspend fun FragmentActivity.pickVisualMediaSuspend(mimeType: String): Uri? {
    return suspendCancellableCoroutine { cont ->
        pickVisualMedia(
            mimeType = mimeType,
            error = {
                if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                    cont.resumeWithException(Throwable(it))
                }
            },
            callback = {
                if (cont.isActive && !(cont.isCancelled || cont.isCompleted)) {
                    cont.resume(it)
                }
            }
        )
    }
}

@MainThread
suspend fun Fragment.pickVisualMediaSuspend(mimeType: String): Uri? {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.pickVisualMediaSuspend(mimeType)
}

@MainThread
suspend fun FragmentActivity.pickImageSuspend(): Uri? {
    return pickVisualMediaSuspend(mimeType = "image/*")
}

@MainThread
suspend fun Fragment.pickImageSuspend(): Uri? {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.pickImageSuspend()
}

@MainThread
suspend fun FragmentActivity.pickVideoSuspend(): Uri? {
    return pickVisualMediaSuspend(mimeType = "video/*")
}

@MainThread
suspend fun Fragment.pickVideoSuspend(): Uri? {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.pickVideoSuspend()
}

@MainThread
suspend fun FragmentActivity.takeAPhotoSuspend(outputUri: Uri): Boolean {
    return suspendCancellableCoroutine { cont ->
        this.takeAPhoto(
            outputFileUri = outputUri,
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
suspend fun Fragment.takeAPhotoSuspend(outputUri: Uri): Boolean {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    return act!!.takeAPhotoSuspend(outputUri)
}