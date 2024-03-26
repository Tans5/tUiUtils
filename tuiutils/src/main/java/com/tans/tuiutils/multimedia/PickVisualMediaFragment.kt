package com.tans.tuiutils.multimedia

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

@Suppress("DEPRECATION")
internal class PickVisualMediaFragment : Fragment {

    private val callback: ((uri: Uri?) -> Unit)?
    private val error: ((msg: String) -> Unit)?

    private val mimeType: String?

    private var lastRequestCode: Int? = null

    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    constructor() {
        this.callback = null
        this.mimeType = null
        this.error = null
    }
    constructor(mimeType: String, error: (msg: String) -> Unit, callback: (uri: Uri?) -> Unit) {
        this.callback = callback
        this.mimeType = mimeType
        this.error = error
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tUiUtilsLog.d(TAG, "Fragment created.")
        val mimeType = mimeType
        val context = activity
        if (mimeType == null) {
            tUiUtilsLog.e(TAG, "Mimetype is null, finish TakeAPhotoFragment.")
            if (hasInvokeCallback.compareAndSet(false, true)) {
                error?.invoke("Mimetype is null.")
            }
            finishCurrentFragment()
            return
        }
        if (context == null) {
            tUiUtilsLog.e(TAG, "Attached activity is null.")
            if (hasInvokeCallback.compareAndSet(false, true)) {
                error?.invoke("Attached activity is null.")
            }
            finishCurrentFragment()
            return
        }

        val intent = when {
            isSystemPickerAvailable() -> {
                Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                    type = mimeType
                }
            }
            isSystemFallbackPickerAvailable(context) -> {
                val fallbackPicker = getSystemFallbackPicker(context)!!.activityInfo
                Intent(ACTION_SYSTEM_FALLBACK_PICK_IMAGES).apply {
                    setClassName(fallbackPicker.applicationInfo.packageName, fallbackPicker.name)
                    type = mimeType
                }
            }
            isGmsPickerAvailable(context) -> {
                val gmsPicker = getGmsPicker(context)!!.activityInfo
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

        val requestCode = Random(System.currentTimeMillis()).nextInt(0, 65535)
        lastRequestCode = requestCode
        startActivityForResult(intent, requestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == lastRequestCode) {
            finishCurrentFragment()
            if (hasInvokeCallback.compareAndSet(false, true)) {
                val uri = data.takeIf { resultCode == Activity.RESULT_OK }?.run {
                    this.data ?: getClipDataUris().firstOrNull()
                }
                callback?.invoke(uri)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasInvokeCallback.compareAndSet(false, true)) {
            error?.invoke("Fragment exit unexpectedly.")
        }
        tUiUtilsLog.d(TAG, "Fragment destroyed.")
    }

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

    private fun finishCurrentFragment() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.remove(this)
        transaction.commitAllowingStateLoss()
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

    companion object {
        private const val TAG = "PickVisualMediaFragment"

        private const val ACTION_SYSTEM_FALLBACK_PICK_IMAGES = "androidx.activity.result.contract.action.PICK_IMAGES"

        private const val GMS_ACTION_PICK_IMAGES = "com.google.android.gms.provider.action.PICK_IMAGES"
    }
}