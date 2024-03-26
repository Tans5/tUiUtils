package com.tans.tuiutils.permission

import androidx.fragment.app.Fragment

internal class PermissionRequestFragment : Fragment {

    private val requestPermissions: Set<String>?
    private val callback: ((granted: Set<String>, notGranted: Set<String>) -> Unit)?

    constructor() {
        this.requestPermissions = null
        this.callback = null
    }

    constructor(requestPermissions: Set<String>, callback: (granted: Set<String>, notGranted: Set<String>) -> Unit) {
        this.requestPermissions = requestPermissions
        this.callback = callback
    }
}