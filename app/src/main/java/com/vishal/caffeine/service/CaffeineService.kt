package com.vishal.caffeine.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.vishal.caffeine.R

class CaffeineService : Service() {
    lateinit var keepScreenOn: PowerManager.WakeLock
    private val CHANNEL: String = "Caffeine"
    private val NOTIFICATION_ID = 100
    var countDownTimer: CountDownTimer? = null
    var mTimerInterFace: TimerInterFace? = null

    inner class QtBinder : Binder() {
        fun getService(): CaffeineService = this@CaffeineService
    }

    interface TimerInterFace {
        fun onTick(millisUntilFinished: Long)
        fun onFinish()
    }

    override fun onCreate() {
        super.onCreate()
        setupWakeLock()
    }

    private fun setupWakeLock() {
        val wake: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        keepScreenOn =
            wake.newWakeLock(PowerManager.FULL_WAKE_LOCK, CaffeineService::class.java.simpleName)
        keepScreenOn.setReferenceCounted(false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return QtBinder()
    }

    private fun showNotification() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, prepareNotification())
    }

    fun stopCaffeine() {
        countDownTimer?.apply {
            cancel()
            onFinish()
        }
        releaseWakeLockAndStopForeground()
    }

    fun releaseWakeLockAndStopForeground() {
        keepScreenOn.release()
        stopForeground(true)
    }

    fun startCaffeine() {
        keepScreenOn.acquire()
        showNotification()
    }

    fun updateCaffeineTimer(timerSeconds: Long) {
        countDownTimer?.cancel()
        setCountDownTimer(timerSeconds)
    }

    private fun setCountDownTimer(timerSeconds: Long) {
        countDownTimer = object : CountDownTimer(timerSeconds, 1000) {
            override fun onFinish() {
                releaseWakeLockAndStopForeground()
                mTimerInterFace?.onFinish()
            }

            override fun onTick(millisUntilFinished: Long) {
                mTimerInterFace?.onTick(millisUntilFinished)
            }

        }
        countDownTimer?.start()
    }

    /*
    Helper functions related to notification
     */
    private fun prepareNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.icon_coffee)
            .setContentTitle("Caffeine")
            .setContentText("Caffeine is running")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL, CHANNEL, importance)
            channel.description = "Notification Channel for Caffeine"
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}