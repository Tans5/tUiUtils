package com.tans.tuiutils.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tans.tuiutils.actresult.startActivityResult
import com.tans.tuiutils.assertMainThread
import com.tans.tuiutils.tUiUtilsLog

private const val ACTION_REQUEST_PERMISSIONS =
    "androidx.activity.result.contract.action.REQUEST_PERMISSIONS"

private const val EXTRA_PERMISSIONS = "androidx.activity.result.contract.extra.PERMISSIONS"

private const val EXTRA_PERMISSION_GRANT_RESULTS =
    "androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS"

@MainThread
fun FragmentActivity.permissionsRequest(vararg permissions: String, error: (msg: String) -> Unit, callback: (granted: Set<String>, notGranted: Set<String>) -> Unit) {
    assertMainThread { "permissionsRequest() need invoke in main thread." }
    if (permissions.isEmpty()) {
        tUiUtilsLog.w(msg = "Request Permission is null, skip request.")
        callback(emptySet(), emptySet())
        return
    }
    val permissionsSet = permissions.toSet()
    val permissionsNeedRequestSet = permissionsSet.filter { !permissionCheck(it) }.toSet()
    val permissionsNotNeedRequestSet = permissionsSet - permissionsNeedRequestSet

    if (permissionsNeedRequestSet.isEmpty()) {
        callback(permissionsSet, emptySet())
        tUiUtilsLog.d(msg = "All permission granted, skip request: $permissionsSet")
        return
    }

    tUiUtilsLog.d(msg = "Permission request: $permissionsNeedRequestSet, skip request: $permissionsNotNeedRequestSet")

    val intent = Intent(ACTION_REQUEST_PERMISSIONS)
        .putExtra(EXTRA_PERMISSIONS, permissionsNeedRequestSet.toTypedArray())

    startActivityResult(
        targetActivityIntent = intent,
        error = error,
        callback = { resultCode: Int, resultData: Intent? ->
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                val resultDataPermissions = resultData.getStringArrayExtra(EXTRA_PERMISSIONS)
                val resultDataGrantResult = resultData.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS)
                if (resultDataPermissions == null || resultDataGrantResult == null) {
                    error("Unknown error.")
                } else {
                    val grantState = resultDataGrantResult.map { it == PackageManager.PERMISSION_GRANTED }
                    val permissionsAndGrantState = resultDataPermissions.filterNotNull().zip(grantState).toMap()
                    val granted = permissionsAndGrantState.filter { it.value }.map { it.key }.toSet() + permissionsNotNeedRequestSet
                    val deny = permissionsAndGrantState.filter { !it.value }.map { it.key }.toSet()
                    callback(granted, deny)
                }
            } else {
                error("Unknown error: $resultCode")
            }
        }
    )

//    val launcher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { result ->
//        val granted = result.filter { it.value }.map { it.key }.toSet() + permissionsNotNeedRequestSet
//        val notGranted = result.filter { !it.value }.map { it.key }.toSet()
//        callback(granted, notGranted)
//    }
//    launcher.launch(permissionsNeedRequestSet.toTypedArray())
}

@MainThread
fun Fragment.permissionsRequest(vararg permissions: String, error: (msg: String) -> Unit, callback: (granted: Set<String>, notGranted: Set<String>) -> Unit) {
    val act = this.activity
    com.tans.tuiutils.assert(act != null) { "Fragment's parent activity is null." }
    act!!.permissionsRequest(permissions = permissions, error = error, callback = callback)
}

@MainThread
fun FragmentActivity.permissionsRequestSimplify(vararg permissions: String, error: (msg: String) -> Unit, callback: (grant: Boolean) -> Unit) {
    permissionsRequest(permissions = permissions, error = error) { _, notGranted ->
        callback(notGranted.isEmpty())
    }
}

fun Context.permissionCheck(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}