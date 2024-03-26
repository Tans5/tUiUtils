package com.tans.tuiutils.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tans.tuiutils.assertMainThread
import com.tans.tuiutils.tUiUtilsLog

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

    val fragment = PermissionRequestFragment(
        requestPermissions = permissionsNeedRequestSet,
        error = error,
        callback = { g, ng ->
            val granted = g + permissionsNotNeedRequestSet
            callback(granted, ng)
        }
    )
    val tc = supportFragmentManager.beginTransaction()
    tc.add(fragment, "PermissionRequestFragment#${System.currentTimeMillis()}")
    tc.commitAllowingStateLoss()

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