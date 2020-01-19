package com.vishal.caffeine.service

import android.content.Intent
import android.os.IBinder
import android.service.quicksettings.TileService
import com.vishal.caffeine.constants.START_CAFFEINE
import com.vishal.caffeine.constants.STOP_CAFFEINE

class QtService : TileService() {
    var state = false
    override fun onClick() {
        val intent = Intent(this, CaffeineService::class.java)
        intent.action = when {
            state -> STOP_CAFFEINE
            else -> START_CAFFEINE
        }
        state = !state
        startService(intent)
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    override fun onTileAdded() {
        super.onTileAdded()
    }

    override fun onStartListening() {
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile;
        tile.label = "Caffeine"
        tile.state = 1;
        tile.updateTile()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}