package com.havanhuan.booster

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.Build
import java.util.Locale

/** Measurements exposed to a normal third-party app (no root or privileged permissions). */
data class DeviceSnapshot(
    val ramAvailableBytes: Long,
    val ramTotalBytes: Long,
    val storageAvailableBytes: Long,
    val storageTotalBytes: Long,
    val batteryPercent: Int?,
    val batteryTemperatureC: Float?,
    val thermalStatus: Int?
) {
    val ramFreePercent: Float
        get() = if (ramTotalBytes > 0L) 100f * ramAvailableBytes / ramTotalBytes else 0f

    companion object {
        fun capture(context: Context): DeviceSnapshot {
            val memoryInfo = ActivityManager.MemoryInfo()
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memoryInfo)
            val storage = StatFs(Environment.getDataDirectory().absolutePath)
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val battery = if (level >= 0 && scale > 0) level * 100 / scale else null
            val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) power.currentThermalStatus else null
            return DeviceSnapshot(
                ramAvailableBytes = memoryInfo.availMem,
                ramTotalBytes = memoryInfo.totalMem,
                storageAvailableBytes = storage.availableBytes,
                storageTotalBytes = storage.totalBytes,
                batteryPercent = battery,
                batteryTemperatureC = if (temp >= 0) temp / 10f else null,
                thermalStatus = thermal
            )
        }
    }
}

fun Long.toSizeLabel(): String {
    val gb = this / (1024.0 * 1024 * 1024)
    return if (gb >= 1.0) String.format(Locale.getDefault(), "%.1f GB", gb)
    else String.format(Locale.getDefault(), "%.0f MB", this / (1024.0 * 1024))
}
