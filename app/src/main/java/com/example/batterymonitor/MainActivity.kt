package com.example.batterymonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences

class MainActivity : AppCompatActivity() {
    private lateinit var batteryProgressBar: ProgressBar
    private lateinit var batteryPercentage: TextView
    private lateinit var batteryStatus: TextView
    private lateinit var batteryTemperature: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createNotificationChannel(this)

        batteryProgressBar = findViewById(R.id.batteryProgressBar)
        batteryPercentage = findViewById(R.id.batteryPercentage)
        batteryStatus = findViewById(R.id.batteryStatus)
        batteryTemperature = findViewById(R.id.batteryTemperature)
        sharedPreferences = getSharedPreferences("BatteryMonitorPrefs", Context.MODE_PRIVATE)

        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private val batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)

            val batteryPct = (level / scale.toFloat() * 100).toInt()
            batteryProgressBar.progress = batteryPct
            batteryPercentage.text = "$batteryPct%"

            val chargingStatus = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                else -> "Discharging"
            }
            batteryStatus.text = chargingStatus

            val tempCelsius = temperature / 10.0f
            batteryTemperature.text = "Temperature: ${tempCelsius}°C"

            val editor = sharedPreferences.edit()

            // LOW BATTERY Notification
            val lowBatteryNotified = sharedPreferences.getBoolean("LowBatteryNotified", false)
            if (batteryPct <= 20 && !lowBatteryNotified) {
                NotificationHelper.showNotification(
                    context,
                    "Low Battery",
                    "Battery level is at $batteryPct%"
                )
                editor.putBoolean("LowBatteryNotified", true)
            } else if (batteryPct > 20 && lowBatteryNotified) {
                // Reset flag when battery increases again
                editor.putBoolean("LowBatteryNotified", false)
            }

            // CHARGING Notification
            val chargingNotified = sharedPreferences.getBoolean("ChargingNotified", false)
            if (status == BatteryManager.BATTERY_STATUS_CHARGING && !chargingNotified) {
                NotificationHelper.showNotification(
                    context,
                    "Charging",
                    "Battery is charging: $batteryPct%"
                )
                editor.putBoolean("ChargingNotified", true)
            } else if (status != BatteryManager.BATTERY_STATUS_CHARGING && chargingNotified) {
                editor.putBoolean("ChargingNotified", false)
            }

            // FULL BATTERY Notification
            val fullNotified = sharedPreferences.getBoolean("FullNotified", false)
            if (status == BatteryManager.BATTERY_STATUS_FULL && !fullNotified) {
                NotificationHelper.showNotification(
                    context,
                    "Battery Full",
                    "Battery is fully charged. Please unplug."
                )
                editor.putBoolean("FullNotified", true)
            } else if (status != BatteryManager.BATTERY_STATUS_FULL && fullNotified) {
                editor.putBoolean("FullNotified", false)
            }

            editor.apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }
}
