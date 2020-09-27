package com.vishal.caffeine.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.vishal.caffeine.R

class CaffeineService : Service() {
    lateinit var wakeLock: PowerManager.WakeLock
    var countDownTimer: CountDownTimer? = null
    var timerInterFace: TimerInterFace? = null

    override fun onCreate() {
        super.onCreate()
        setupWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return QtBinder()
    }

    fun stopCaffeine() {
        countDownTimer?.apply {
            cancel()
            onFinish()
        }
        releaseWakeLockAndStopForeground()
    }

    fun releaseWakeLockAndStopForeground() {
        wakeLock.release()
        stopForeground(true)
    }

    @SuppressLint("WakelockTimeout")
    fun startCaffeine() {
        wakeLock.acquire()
        showNotification()
    }

    fun updateCaffeineTimer(timerSeconds: Long) {
        countDownTimer?.cancel()
        setCountDownTimer(timerSeconds)
    }

    private fun setupWakeLock() {
        val powerManager: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK,
            CaffeineService::class.java.simpleName
        ).apply { setReferenceCounted(false) }
    }

    private fun showNotification() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, prepareNotification())
    }

    private fun setCountDownTimer(timerSeconds: Long) {
        countDownTimer = object : CountDownTimer(timerSeconds, 1000) {
            override fun onFinish() {
                releaseWakeLockAndStopForeground()
                timerInterFace?.onFinish()
            }

            override fun onTick(millisUntilFinished: Long) {
                timerInterFace?.onTick(millisUntilFinished)
            }
        }.start()
    }

    @CheckResult
    private fun prepareNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.icon_coffee)
            .setContentTitle("Caffeine")
            .setContentText("Caffeine is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL, CHANNEL, importance).apply {
                description = "Notification Channel for Caffeine"
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    inner class QtBinder : Binder() {
        @CheckResult
        fun getService(): CaffeineService = this@CaffeineService
    }

    interface TimerInterFace {
        fun onTick(millisUntilFinished: Long)
        fun onFinish()
    }

    companion object {
        private const val CHANNEL: String = "Caffeine"
        private const val NOTIFICATION_ID = 100
    }
}