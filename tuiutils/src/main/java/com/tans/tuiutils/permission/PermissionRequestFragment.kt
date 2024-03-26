package com.tans.tuiutils.permission

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.tans.tuiutils.tUiUtilsLog
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

@Suppress("DEPRECATION")
internal class PermissionRequestFragment : Fragment {

    private val requestPermissions: Set<String>?
    private val callback: ((granted: Set<String>, notGranted: Set<String>) -> Unit)?
    private val error: ((msg: String) -> Unit)?

    private val hasInvokeCallback: AtomicBoolean = AtomicBoolean(false)

    private var lastRequestCode: Int? = null

    constructor() {
        this.requestPermissions = null
        this.callback = null
        this.error = null
    }

    constructor(requestPermissions: Set<String>, error: (msg: String) -> Unit, callback: (granted: Set<String>, notGranted: Set<String>) -> Unit) {
        this.requestPermissions = requestPermissions
        this.callback = callback
        this.error = error
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tUiUtilsLog.d(TAG, "Fragment created.")
        val requestPermissions = requestPermissions
        val context = activity
        if (requestPermissions == null) {
            tUiUtilsLog.e(TAG, "RequestPermissions is null.")
            if (hasInvokeCallback.compareAndSet(false, true)) {
                error?.invoke("RequestPermissions is null.")
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
        val intent = Intent(ACTION_REQUEST_PERMISSIONS)
            .putExtra(EXTRA_PERMISSIONS, requestPermissions.toTypedArray())

        val requestCode = Random(System.currentTimeMillis()).nextInt(0, 65535)
        lastRequestCode = requestCode
        startActivityForResult(intent, requestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == lastRequestCode) {
            finishCurrentFragment()
            if (hasInvokeCallback.compareAndSet(false, true)) {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val permissions = data.getStringArrayExtra(EXTRA_PERMISSIONS)
                    val grantResults = data.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS)
                    if (permissions == null || grantResults == null) {
                        error?.invoke("Unknown error.")
                    } else {
                       val grantState = grantResults.map { it == PackageManager.PERMISSION_GRANTED }
                        val permissionsAndGrantState = permissions.filterNotNull().zip(grantState).toMap()
                        callback?.invoke(permissionsAndGrantState.filter { it.value }.map { it.key }.toSet(),
                            permissionsAndGrantState.filter { !it.value }.map { it.key }.toSet())
                    }
                } else {
                    error?.invoke("Unknown error: $resultCode")
                }
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
        private const val ACTION_REQUEST_PERMISSIONS =
            "androidx.activity.result.contract.action.REQUEST_PERMISSIONS"

        private const val EXTRA_PERMISSIONS = "androidx.activity.result.contract.extra.PERMISSIONS"

        private const val EXTRA_PERMISSION_GRANT_RESULTS =
            "androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS"

        private const val TAG = "PermissionRequestFragment"

    }
}