package com.tans.tuiutils.multimedia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

@Suppress("DEPRECATION")
internal class TakeAPhotoFragment : Fragment {

    private val outputUri: Uri?

    private val callback: ((isOk: Boolean) -> Unit)?

    private val error: ((msg: String) -> Unit)?

    private var lastRequestCode: Int? = null

    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    constructor() {
        this.outputUri = null
        this.callback = null
        this.error = null
    }
    constructor(outputUri: Uri, error: (msg: String) -> Unit, callback: (isOk: Boolean) -> Unit) {
        this.outputUri = outputUri
        this.callback = callback
        this.error = error
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tUiUtilsLog.d(TAG, "Fragment created.")
        val outputUri = outputUri
        val context = activity
        if (outputUri == null) {
            tUiUtilsLog.e(TAG, "Output Uri is null, finish TakeAPhotoFragment.")
            if (hasInvokeCallback.compareAndSet(false, true)) {
                error?.invoke("Output Uri is null.")
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
            if (hasInvokeCallback.compareAndSet(false, true)) {
                error?.invoke("No activity can take photo.")
            }
            finishCurrentFragment()
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.fragment.app.Fragment")
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == lastRequestCode) {
            if (hasInvokeCallback.compareAndSet(false, true)) {
                callback?.invoke(resultCode == Activity.RESULT_OK)
            }
            finishCurrentFragment()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasInvokeCallback.compareAndSet(false, true)) {
            error?.invoke("Fragment exit unexpectedly.")
        }
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