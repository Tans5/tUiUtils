package com.tans.tuiutils.multimedia

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tans.tuiutils.actresult.startActivityResult
import com.tans.tuiutils.assertMainThread

private const val ACTION_SYSTEM_FALLBACK_PICK_IMAGES = "androidx.activity.result.contract.action.PICK_IMAGES"

private const val GMS_ACTION_PICK_IMAGES = "com.google.android.gms.provider.action.PICK_IMAGES"

private fun isSystemPickerAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        true
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // getExtension is seen as part of Android Tiramisu only while the SdkExtensions
        // have been added on Android R
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2
    } else {
        false
    }
}

private fun isSystemFallbackPickerAvailable(context: Context): Boolean {
    return getSystemFallbackPicker(context) != null
}

private fun getSystemFallbackPicker(context: Context): ResolveInfo? {
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY
    } else {
        PackageManager.MATCH_DEFAULT_ONLY
    }
    return context.packageManager.resolveActivity(
        Intent(ACTION_SYSTEM_FALLBACK_PICK_IMAGES),
        flags
    )
}

private fun isGmsPickerAvailable(context: Context): Boolean {
    return getGmsPicker(context) != null
}
private fun getGmsPicker(context: Context): ResolveInfo? {
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY
    } else {
        PackageManager.MATCH_DEFAULT_ONLY
    }
    return context.packageManager.resolveActivity(
        Intent(GMS_ACTION_PICK_IMAGES),
        flags
    )
}


private fun Intent.getClipDataUris(): List<Uri> {
    // Use a LinkedHashSet to maintain any ordering that may be
    // present in the ClipData
    val resultSet = LinkedHashSet<Uri>()
    data?.let { data ->
        resultSet.add(data)
    }
    val clipData = clipData
    if (clipData == null && resultSet.isEmpty()) {
        return emptyList()
    } else if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                resultSet.add(uri)
            }
        }
    }
    return ArrayList(resultSet)
}

@SuppressLint("InlinedApi")
@MainThread
fun FragmentActivity.pickVisualMedia(mimeType: String, error: (msg: String) -> Unit, callback: (uri: Uri?) -> Unit) {
    assertMainThread { "pickVisualMedia() need invoke in main thread." }
//    val launcher = registerForActivityResult(
//        ActivityResultContracts.PickVisualMedia()
//    ) { uri ->
//        callback(uri)
//    }
//    launcher.launch(PickVisualMediaRequest(type))

    val intent = when {
        isSystemPickerAvailable() -> {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = mimeType
            }
        }
        isSystemFallbackPickerAvailable(this) -> {
            val fallbackPicker = getSystemFallbackPicker(this)!!.activityInfo
            Intent(ACTION_SYSTEM_FALLBACK_PICK_IMAGES).apply {
                setClassName(fallbackPicker.applicationInfo.packageName, fallbackPicker.name)
                type = mimeType
            }
        }
        isGmsPickerAvailable(this) -> {
            val gmsPicker = getGmsPicker(this)!!.activityInfo
            Intent(GMS_ACTION_PICK_IMAGES).apply {
                setClassName(gmsPicker.applicationInfo.packageName, gmsPicker.name)
                type = mimeType
            }
        }
        else -> {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = mimeType
            }
        }
    }

    startActivityResult(
        targetActivityIntent = intent,
        error = error,
        callback = { resultCode: Int, resultData: Intent? ->
            val uri = resultData.takeIf { resultCode == Activity.RESULT_OK }?.run {
                this.data ?: getClipDataUris().firstOrNull()
            }
            callback.invoke(uri)
        }
    )
}

@MainThread
fun Fragment.pickVisualMedia(mimeType: String, error: (msg: String) -> Unit, callback: (uri: Uri?) -> Unit) {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.pickVisualMedia(mimeType, error, callback)
}

@MainThread
fun FragmentActivity.pickImage(error: (msg: String) -> Unit, callback: (uri: Uri?) -> Unit) {
    pickVisualMedia(mimeType = "image/*", error = error, callback = callback)
}

@MainThread
fun Fragment.pickImage(error: (msg: String) -> Unit, callback: (uri: Uri?) -> Unit) {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.pickImage(error, callback)
}

@MainThread
fun FragmentActivity.pickVideo(error: (msg: String) -> Unit, callback: (uri: Uri?) -> Unit) {
    pickVisualMedia(mimeType = "video/*", error = error, callback = callback)
}

@MainThread
fun Fragment.pickVideo(error: (msg: String) -> Unit, callback: (uri: Uri?) -> Unit) {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.pickVideo(error, callback)
}


/**
 * Need FileProvider convert OutputFile to uri，See: https://stackoverflow.com/questions/18249007/how-to-use-support-fileprovider-for-sharing-content-to-other-apps
 *
 */
@MainThread
fun FragmentActivity.takeAPhoto(outputFileUri: Uri, error: (msg: String) -> Unit, callback: (success: Boolean) -> Unit) {
    assertMainThread { "takeAPhoto() need invoke in main thread." }
//    val launcher = registerForActivityResult(
//        ActivityResultContracts.TakePicture()) {
//        callback(it)
//    }
//    launcher.launch(outputFileUri)
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
    startActivityResult(
        targetActivityIntent = intent,
        error = error,
        callback = { resultCode: Int, _ ->
            callback(resultCode == Activity.RESULT_OK)
        }
    )
}

@MainThread
fun Fragment.takeAPhoto(outputFileUri: Uri, error: (msg: String) -> Unit, callback: (success: Boolean) -> Unit) {
    val act = activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.takeAPhoto(outputFileUri, error, callback)
}