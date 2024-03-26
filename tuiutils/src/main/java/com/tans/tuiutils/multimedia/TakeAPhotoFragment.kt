package com.tans.tuiutils.multimedia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.tans.tuiutils.tUiUtilsLog
import kotlin.random.Random

@Suppress("DEPRECATION")
internal class TakeAPhotoFragment : Fragment {

    private val outputUri: Uri?

    private val callback: ((isOk: Boolean) -> Unit)?

    private var lastRequestCode: Int? = null

    constructor() {
        this.outputUri = null
        this.callback = null
    }
    constructor(outputUri: Uri, callback: (isOk: Boolean) -> Unit) {
        this.outputUri = outputUri
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tUiUtilsLog.d(TAG, "Fragment created.")
        val outputUri = outputUri
        val context = activity
        if (outputUri == null) {
            tUiUtilsLog.e(TAG, "Output Uri is null, finish TakeAPhotoFragment.")
            finishCurrentFragment()
            callback?.invoke(false)
            return
        }
        if (context == null) {
            tUiUtilsLog.e(TAG, "Attached activity is null.")
            callback?.invoke(false)
            return
        }

        val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        val pm = context.packageManager
        val infos = pm.queryIntentActivities(i, 0)
        if (infos.isNotEmpty()) {
            val requestCode = Random(System.currentTimeMillis()).nextInt(0, 65535)
            lastRequestCode = requestCode
            startActivityForResult(i, requestCode)
        } else {
            tUiUtilsLog.e(TAG, "No activity can take photo, exit.")
            finishCurrentFragment()
            callback?.invoke(false)
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.fragment.app.Fragment")
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == lastRequestCode) {
            callback?.invoke(resultCode == Activity.RESULT_OK)
            finishCurrentFragment()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tUiUtilsLog.d(TAG, "Fragment destroyed.")
    }

    private fun finishCurrentFragment() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.remove(this)
        transaction.commitAllowingStateLoss()
    }

    companion object {
        private const val TAG = "TakeAPhotoFragment"
    }
}