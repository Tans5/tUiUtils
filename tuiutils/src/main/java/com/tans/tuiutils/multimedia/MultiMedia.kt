package com.tans.tuiutils.multimedia

import android.net.Uri
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import com.tans.tuiutils.assertMainThread

@MainThread
fun FragmentActivity.pickVisualMedia(mimeType: String, callback: (uri: Uri?) -> Unit) {
    assertMainThread { "pickVisualMedia() need invoke in main thread." }
//    val launcher = registerForActivityResult(
//        ActivityResultContracts.PickVisualMedia()
//    ) { uri ->
//        callback(uri)
//    }
//    launcher.launch(PickVisualMediaRequest(type))
    val fragment = PickVisualMediaFragment(
        mimeType = mimeType,
    ) { callback(it) }
    val tc = supportFragmentManager.beginTransaction()
    tc.add(fragment, "PickVisualMediaFragment#${System.currentTimeMillis()}")
    tc.commitAllowingStateLoss()
}

@MainThread
fun FragmentActivity.pickImage(callback: (uri: Uri?) -> Unit) {
    pickVisualMedia(mimeType = "image/*", callback = callback)
}

@MainThread
fun FragmentActivity.pickVideo(callback: (uri: Uri?) -> Unit) {
    pickVisualMedia(mimeType = "video/*", callback = callback)
}


/**
 * Need FileProvider convert OutputFile to uri，See: https://stackoverflow.com/questions/18249007/how-to-use-support-fileprovider-for-sharing-content-to-other-apps
 *
 */
@MainThread
fun FragmentActivity.takeAPhoto(outputFileUri: Uri, callback: (success: Boolean) -> Unit) {
    assertMainThread { "takeAPhoto() need invoke in main thread." }
//    val launcher = registerForActivityResult(
//        ActivityResultContracts.TakePicture()) {
//        callback(it)
//    }
//    launcher.launch(outputFileUri)
    val tc = supportFragmentManager.beginTransaction()
    val fragment = TakeAPhotoFragment(
        outputUri = outputFileUri
    ) { isOk ->
        callback(isOk)
    }
    tc.add(fragment, "TakeAPhotoFragment#${System.currentTimeMillis()}")
    tc.commitAllowingStateLoss()
}