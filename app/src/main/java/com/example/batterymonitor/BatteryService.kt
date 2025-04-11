package com.example.batterymonitor

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder

class BatteryService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val batteryLevel = getBatteryLevel()
        println("Battery Level: $batteryLevel%") // Log the battery level
        return START_STICKY
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
