package com.tans.tuiutils.multimedia

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import com.tans.tuiutils.assertMainThread

@MainThread
fun ComponentActivity.pickVisualMedia(type: ActivityResultContracts.PickVisualMedia.VisualMediaType, callback: (uri: Uri?) -> Unit) {
    assertMainThread { "pickVisualMedia() need invoke in main thread." }
    val launcher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        callback(uri)
    }
    launcher.launch(PickVisualMediaRequest(type))
}

@MainThread
fun ComponentActivity.pickImage(callback: (uri: Uri?) -> Unit) {
    pickVisualMedia(type = ActivityResultContracts.PickVisualMedia.ImageOnly, callback = callback)
}

@MainThread
fun ComponentActivity.pickVideo(callback: (uri: Uri?) -> Unit) {
    pickVisualMedia(type = ActivityResultContracts.PickVisualMedia.VideoOnly, callback = callback)
}


/**
 * Need FileProvider convert OutputFile to uri，See: https://stackoverflow.com/questions/18249007/how-to-use-support-fileprovider-for-sharing-content-to-other-apps
 *
 */
@MainThread
fun ComponentActivity.takeAPhoto(outputFileUri: Uri, callback: (success: Boolean) -> Unit) {
    assertMainThread { "takeAPhoto() need invoke in main thread." }
    val launcher = registerForActivityResult(
        ActivityResultContracts.TakePicture()) {
        callback(it)
    }
    launcher.launch(outputFileUri)

}