package com.tans.tuiutils.demo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.tans.tuiutils.clicks.clicks
import com.tans.tuiutils.demo.databinding.DialogYesOrNoBinding
import com.tans.tuiutils.dialog.BaseCoroutineStateForceResultDialogFragment
import com.tans.tuiutils.dialog.DialogForceResultCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class YesOrNoDialog : BaseCoroutineStateForceResultDialogFragment<Unit, Boolean> {

    constructor() : super(Unit, null)
    constructor(callback: DialogForceResultCallback<Boolean>) : super(Unit, callback)

    override fun createContentView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.dialog_yes_or_no, parent, false)
    }

    override fun firstLaunchInitData() {
    }

    override fun bindContentView(view: View) {
        val viewBinding = DialogYesOrNoBinding.bind(view)

        viewBinding.yesBt.clicks(this) {
            onResult(true)
        }

        viewBinding.noBt.clicks(this) {
            onResult(false)
        }
    }
}

suspend fun FragmentManager.showYesOrNoDialogSuspend(): Boolean {
    return suspendCancellableCoroutine<Boolean> { cont ->
        val dialog = YesOrNoDialog(callback = object : DialogForceResultCallback<Boolean> {
            override fun onResult(t: Boolean) {
                if (cont.isActive) {
                    cont.resume(t)
                }
            }

            override fun onError(e: String) {
                if (cont.isActive) {
                    cont.resumeWithException(Throwable(e))
                }
            }
        })
        dialog.show(this, "YesOrNoDialog#${System.currentTimeMillis()}")
        val dialogWeak = WeakReference(dialog)
        cont.invokeOnCancellation {
            val d = dialogWeak.get()
            if (d != null && d.isVisible) {
                d.dismissSafe()
            }
        }
    }
}