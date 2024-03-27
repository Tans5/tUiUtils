package com.tans.tuiutils.actresult

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

@Suppress("DEPRECATION")
internal class ActivityResultFragment : Fragment {

    private val callback: ((resultCode: Int, resultData: Intent?) -> Unit)?
    private val error: ((msg: String) -> Unit)?
    private val targetActivityIntent: Intent?

    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    private var lastRequestCode: Int? = null

    constructor() {
        this.callback = null
        this.error = null
        this.targetActivityIntent = null
    }

    constructor(targetActivityIntent: Intent, error: (msg: String) -> Unit, callback: (resultCode: Int, resultData: Intent?) -> Unit) {
        this.targetActivityIntent = targetActivityIntent
        this.error = error
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tUiUtilsLog.d(TAG, "Fragment created.")
        val context = activity
        val targetActivityIntent = this.targetActivityIntent
        if (targetActivityIntent == null) {
            tUiUtilsLog.e(TAG, "Target activity intent is null.")
            if (hasInvokeCallback.compareAndSet(false, true)) {
                error?.invoke("Target activity intent is null.")
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
        }

        val requestCode = Random(System.currentTimeMillis()).nextInt(0, 65535)
        lastRequestCode = requestCode
        startActivityForResult(targetActivityIntent, requestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == lastRequestCode) {
            finishCurrentFragment()
            if (hasInvokeCallback.compareAndSet(false, true)) {
                callback?.invoke(resultCode, data)
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

    private fun finishCurrentFragment() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.remove(this)
        transaction.commitAllowingStateLoss()
    }

    companion object {
        private const val TAG = "ActivityResultFragment"
    }
}