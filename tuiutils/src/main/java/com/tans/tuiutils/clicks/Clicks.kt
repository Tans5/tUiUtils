package com.tans.tuiutils.clicks

import android.os.SystemClock
import android.view.View
import com.tans.tuiutils.R
import com.tans.tuiutils.tUiUtilsLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private const val TAG = "tUiUtilsClicks"

data class TimeInterval<E>(val interval: Long, val element: E)

fun <T> Flow<T>.timeInterval(): Flow<TimeInterval<T>> = flow<TimeInterval<T>> {
    var lastElementAndReceiveTime: Pair<T, Long>? = null
    collect { e ->
        val now = SystemClock.uptimeMillis()
        val timeInterval = if (lastElementAndReceiveTime == null) {
            TimeInterval(interval = Long.MAX_VALUE, element = e)
        } else {
            TimeInterval(interval = (lastElementAndReceiveTime?.let { now - it.second } ?: Long.MAX_VALUE), element = e)
        }
        emit(timeInterval)
        lastElementAndReceiveTime = e to now
    }
}

fun View.clicks(
    coroutineScope: CoroutineScope,
    minInterval: Long = 300,
    click: suspend () -> Unit
) {
    val lastJob = this.getTag(R.id.tui_clicks_job_id)
    if (lastJob != null && lastJob is Job) {
        tUiUtilsLog.d(tag = TAG, msg = "Find last click job and cancel it.")
        lastJob.cancel("Cancel from new clicks.")
        this.setTag(R.id.tui_clicks_job_id, null)
    }
    val job = coroutineScope.launch {
        val channel = Channel<Unit>(capacity = Channel.CONFLATED)
        setOnClickListener {
            val sendResult = channel.trySend(Unit)
            tUiUtilsLog.d(tag = TAG, msg = "Click send result: $sendResult")
        }
        val clickFlow = flow {
           this.emitAll(channel)
        }
        try {
            clickFlow
                .conflate() // if collect is busy, drop oldest element
                .timeInterval()
                .filter {
                    val isOk = it.interval >= minInterval
                    if (isOk) {
                        tUiUtilsLog.w(tag = TAG, msg = "Skip click, interval is ${it.interval}ms, min interval is ${minInterval}ms")
                    }
                    isOk
                }
                .collect {
                    click()
                }
        } catch (e: Throwable) {
            tUiUtilsLog.d(tag = TAG, msg = "Click job finished: ${e.message}")
        }
        channel.cancel()
    }
    this.setTag(R.id.tui_clicks_job_id, job)
}