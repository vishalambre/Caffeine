package com.vishal.caffeine.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.vishal.caffeine.R
import com.vishal.caffeine.constants.START_CAFFEINE
import com.vishal.caffeine.constants.STOP_CAFFEINE
import java.util.*

class CaffeineService : Service() {
    lateinit var keepScreenOn: PowerManager.WakeLock
    val CHANNEL: String = "Caffeine"
    val NOTIFICATION_ID = 100
    val handler: Handler = Handler()
    var countDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        val wake: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        keepScreenOn =
            wake.newWakeLock(PowerManager.FULL_WAKE_LOCK, CaffeineService::class.java.simpleName)
        keepScreenOn.setReferenceCounted(false)
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(START_CAFFEINE))
            startCaffeine()
        else stopCaffeine()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun showNotification() {
        createNotificationChannel()
        handler.postDelayed(Runnable { stopCaffeine() }, 5 * 60 * 1000)
        startForeground(NOTIFICATION_ID, prepareNotification())
    }

    private fun stopCaffeine() {
        Toast.makeText(this, "Stop caffeine called", Toast.LENGTH_SHORT).show()
        keepScreenOn.release()
        stopSelf()
    }

    private fun startCaffeine() {
        Toast.makeText(this, "Start caffeine called", Toast.LENGTH_SHORT).show()
        keepScreenOn.acquire()
        countDownTimer?.cancel()
        setCountDownTimer(5*60*1000)
        showNotification()
    }

    private fun setCountDownTimer(timerSeconds: Long) {
         countDownTimer = object : CountDownTimer(timerSeconds, 1000) {
            override fun onFinish() {
                stopCaffeine()
            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }
    }

    private fun prepareNotification(): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.icon_coffee)
            .setContentTitle("Caffeine")
            .setContentText("Click to turn off caffeine")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
//            .setContentIntent(getPendingIntent())
            .build()

        return notification
    }

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getService(
            this,
            1,
            Intent(this, CaffeineService::class.java).apply { action = STOP_CAFFEINE },
            PendingIntent.FLAG_ONE_SHOT
        )

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