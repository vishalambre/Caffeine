package com.vishal.caffeine.service

import android.content.*
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.vishal.caffeine.R
import com.vishal.caffeine.constants.*
import com.vishal.caffeine.toMilliSeconds
import com.vishal.caffeine.toTime

class QtService : TileService(), CaffeineService.TimerInterFace {
    private val screenLockedBroadCast = ScreenLockedBroadCast()
    private var isScreenOff = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            resetTile()
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val qtBinder = binder as CaffeineService.QtBinder
            caffeineService = qtBinder.getService()
            caffeineService.timerInterFace = this@QtService
        }
    }
    private var state = STOPPED_CAFFEINE
    private var isListening: Boolean = false
    private lateinit var caffeineService: CaffeineService

    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, CaffeineService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onClick() {
        state = getNewState(state)
        when (state) {
            INFINITY -> startCaffeine()
            STOPPED_CAFFEINE -> stopCaffeine()
            else -> updateCaffeineTimer(valueMap.getOrDefault(state, 0))
        }
    }

    private fun startCaffeine() {
        caffeineService.startCaffeine()
        updateTile(label = INFINITY_UNICODE)
    }

    private fun stopCaffeine() {
        caffeineService.stopCaffeine()
    }

    private fun updateCaffeineTimer(minutes: Int) {
        caffeineService.updateCaffeineTimer(minutes.toMilliSeconds())
    }

    override fun onStartListening() {
        isListening = true
    }

    override fun onStopListening() {
        isListening = false
    }

    private fun updateTile(
        state: Int = Tile.STATE_ACTIVE,
        label: String = resources.getString(R.string.app_name)
    ) {
        qsTile.label = label
        qsTile.state = state
        qsTile.updateTile()
    }

    override fun onTick(millisUntilFinished: Long) {
        if (isListening) updateTile(label = millisUntilFinished.toTime())
    }

    override fun onFinish() {
        resetTile()
    }

    private fun resetTile() {
        state = STOPPED_CAFFEINE
        updateTile(
            when {
                isScreenOff -> Tile.STATE_UNAVAILABLE
                else -> Tile.STATE_INACTIVE
            }
        )
    }

    override fun onTileAdded() {
        updateTile(state = Tile.STATE_INACTIVE)
        registerScreenLockedBroadCastReceiver()
    }

    override fun onTileRemoved() {
        unregisterScreenLockedBroadCastReceiver()
        super.onTileRemoved()
    }

    private fun registerScreenLockedBroadCastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenLockedBroadCast, intentFilter)
    }

    private fun unregisterScreenLockedBroadCastReceiver() {
        unregisterReceiver(screenLockedBroadCast)
    }

    inner class ScreenLockedBroadCast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOff = true
                    stopCaffeine()
                    resetTile()
                }
                Intent.ACTION_USER_PRESENT -> {
                    isScreenOff = false
                    updateTile(Tile.STATE_INACTIVE)
                }
            }
        }
    }
}