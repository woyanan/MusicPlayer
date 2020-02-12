package com.atlasv.android.music.music_player.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Created by woyanan on 2020-02-12
 */
class TimerTaskManager {
    companion object {
        private const val PROGRESS_UPDATE_INTERNAL = 1000L
        private const val PROGRESS_UPDATE_INITIAL_INTERVAL = 100L
    }

    private val handler = Handler()
    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private var scheduleFuture: ScheduledFuture<*>? = null
    private var progressRunnable: Runnable? = null
    private var time: Long = 0

    fun setUpdateProgress(runnable: Runnable?) {
        progressRunnable = runnable
    }

    fun updateProgress() {
        stopProgress()
        if (!executorService.isShutdown) {
            scheduleFuture = executorService.scheduleAtFixedRate(
                {
                    if (progressRunnable != null) {
                        handler.post(progressRunnable)
                    }
                },
                PROGRESS_UPDATE_INITIAL_INTERVAL,
                PROGRESS_UPDATE_INTERNAL,
                TimeUnit.MILLISECONDS
            )
        }
    }

    fun stopProgress() {
        scheduleFuture?.cancel(false)
    }

    fun startCountDown(
        millisInFuture: Long,
        listener: OnCountDownFinishListener
    ) {
        if (timerHandler == null) {
            timerHandler = Handler(Looper.getMainLooper())
        }
        if (millisInFuture != -1L && millisInFuture > 0L) {
            if (timerRunnable == null) {
                time = millisInFuture
                timerRunnable = Runnable {
                    time -= 1000L
                    listener.onTick(time)
                    if (time <= 0L) {
                        listener.onFinish()
                        cancel()
                    } else {
                        timerHandler!!.postDelayed(timerRunnable, 1000L)
                    }
                }
            }
            timerHandler!!.postDelayed(timerRunnable, 1000L)
        }
    }

    private fun cancel() {
        time = 0
        timerHandler?.removeCallbacksAndMessages(null)
        timerHandler = null
        timerRunnable = null
    }

    fun release() {
        stopProgress()
        executorService.shutdown()
        handler.removeCallbacksAndMessages(null)
    }

    interface OnCountDownFinishListener {
        fun onFinish()
        fun onTick(millisUntilFinished: Long)
    }
}