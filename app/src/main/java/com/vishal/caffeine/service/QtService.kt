package com.vishal.caffeine.service

import android.content.*
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.vishal.caffeine.constants.*
import com.vishal.caffeine.toMilliSeconds
import com.vishal.caffeine.toTime

class QtService : TileService(), CaffeineService.TimerInterFace {
    var state = STOP_CAFFEINE
    var isListening: Boolean = false
    var mCaffeineService: CaffeineService? = null
    val screenLockedBroadCast = ScreenLockedBroadCast()
    var isScreenOff = false

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val qtBinder = service as CaffeineService.QtBinder
            mCaffeineService = qtBinder.getService()
            mCaffeineService?.mTimerInterFace = this@QtService
        }
    }

    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, CaffeineService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onClick() {
        state = getNewState(state)
        when (state) {
            INFINITY -> startCaffeine()
            STOP_CAFFEINE -> stopCaffeine()
            else -> updateCaffeineTimer(valueMap[state] ?: 0)
        }
    }

    private fun startCaffeine() {
        mCaffeineService?.startCaffeine()
        updateTile(label = INFINITY_UNICODE)
    }

    private fun stopCaffeine() {
        mCaffeineService?.stopCaffeine()
    }

    private fun updateCaffeineTimer(minutes: Int) {
        mCaffeineService?.updateCaffeineTimer(minutes.toMilliSeconds())
    }

    override fun onStartListening() {
        isListening = true
    }

    override fun onStopListening() {
        isListening = false
    }

    private fun updateTile(state: Int = Tile.STATE_ACTIVE, label: String = "Caffeine") {
        val tile = qsTile;
        tile.label = label
        tile.state = state
        tile.updateTile()
    }

    override fun onTick(millisUntilFinished: Long) {
        if (isListening) {
            updateTile(label = millisUntilFinished.toTime())
        }
    }

    override fun onFinish() {
        resetTile()
    }

   private fun resetTile() {
        state = STOP_CAFFEINE
        updateTile(
            when {
                isScreenOff -> Tile.STATE_UNAVAILABLE
                else -> Tile.STATE_INACTIVE
            }
        )
    }

    override fun onTileAdded() {
        updateTile(state = Tile.STATE_INACTIVE)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenLockedBroadCast, intentFilter)
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
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