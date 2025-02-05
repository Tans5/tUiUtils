package com.tans.tuiutils.view

import androidx.annotation.MainThread
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tans.tuiutils.R
import com.tans.tuiutils.assertMainThread
import com.tans.tuiutils.tUiUtilsLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private const val TAG = "tUiUtilsRefresh"

@MainThread
fun SwipeRefreshLayout.refreshes(
    coroutineScope: CoroutineScope,
    refreshWorkOn: CoroutineContext = EmptyCoroutineContext,
    parallelWork: Boolean = false,
    refresh: suspend () -> Unit
) {
    assertMainThread { "Refresh must invoke on main thread." }
    val lastClickTask = this.getTag(R.id.tui_refresh_job_id)
    if (lastClickTask != null && lastClickTask is ViewTask) {
        tUiUtilsLog.d(tag = TAG, msg = "Find last click task and cancel it.")
        lastClickTask.clickChannel.cancel()
        if (lastClickTask.clickJob.isActive) {
            lastClickTask.clickJob.cancel("Cancel from new refreshes.")
        }
        this.setTag(R.id.tui_refresh_job_id, null)
    }

    val channel = Channel<Unit>(
        capacity = Channel.RENDEZVOUS,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ) {
        tUiUtilsLog.w(tag = TAG, msg = "Drop refresh event.")
    }

    val job = coroutineScope.launch {
        this@refreshes.setOnRefreshListener {
            channel.trySend(Unit)
        }
        val refreshFlow = flow { this.emitAll(channel) }

        val refreshJobs = mutableListOf<Job>()
        try {
            refreshFlow.collect {
                // Remove finished jobs.
                fun cleanFinishedJobs() {
                    val jobIterator = refreshJobs.iterator()
                    while (jobIterator.hasNext()) {
                        if (!jobIterator.next().isActive) {
                            jobIterator.remove()
                        }
                    }
                }
                cleanFinishedJobs()

                if (!parallelWork) {
                    if (refreshJobs.isNotEmpty()) {
                        tUiUtilsLog.w(
                            tag = TAG,
                            msg = "Last refresh don't completed, skip current job."
                        )
                    } else {
                        launch(refreshWorkOn) {
                            refresh()
                            withContext(Dispatchers.Main.immediate) {
                                if (this@refreshes.isRefreshing) {
                                    this@refreshes.isRefreshing = false
                                }
                            }
                        }.apply { refreshJobs.add(this) }
                    }
                } else {
                    launch(refreshWorkOn) {
                        refresh()
                        cleanFinishedJobs()
                        if (refreshJobs.isEmpty()) {
                            withContext(Dispatchers.Main.immediate) {
                                if (this@refreshes.isRefreshing) {
                                    this@refreshes.isRefreshing = false
                                }
                            }
                        }
                    }.apply { refreshJobs.add(this) }
                }
            }
        } catch (_: Throwable) {

        }
    }
    this.setTag(R.id.tui_refresh_job_id, job)
}