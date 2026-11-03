package com.example.domain.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.example.data.model.DeviceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DeviceStatusRepository(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun getStatusFlow(pollIntervalMs: Long = 6000L): Flow<DeviceStatus> = flow {
        while (true) {
            emit(fetchCurrentStatus())
            delay(pollIntervalMs)
        }
    }.flowOn(Dispatchers.IO)

    fun fetchCurrentStatus(): DeviceStatus {
        // Battery
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale) * 100).toInt() else 0

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val batteryTemp = if (tempTenths > 0) tempTenths / 10f else null

        // Memory / RAM
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalRamBytes = memoryInfo.totalMem
        val availRamBytes = memoryInfo.availMem
        val usedRamBytes = totalRamBytes - availRamBytes

        val totalRamGb = totalRamBytes / (1024f * 1024f * 1024f)
        val usedRamGb = usedRamBytes / (1024f * 1024f * 1024f)
        val freeRamGb = availRamBytes / (1024f * 1024f * 1024f)
        val ramUsagePct = if (totalRamBytes > 0) ((usedRamBytes.toDouble() / totalRamBytes) * 100).toInt() else 0

        // Storage
        val statFs = StatFs(Environment.getDataDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong

        val totalStorageBytes = totalBlocks * blockSize
        val availStorageBytes = availableBlocks * blockSize
        val usedStorageBytes = totalStorageBytes - availStorageBytes

        val totalStorageGb = totalStorageBytes / (1024f * 1024f * 1024f)
        val usedStorageGb = usedStorageBytes / (1024f * 1024f * 1024f)
        val storageUsagePct = if (totalStorageBytes > 0) ((usedStorageBytes.toDouble() / totalStorageBytes) * 100).toInt() else 0

        // Network
        var networkType = "Offline"
        var isConnected = false
        var networkDetails = "No active connection"

        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            isConnected = true
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    networkType = "Wi-Fi"
                    val linkDown = caps.linkDownstreamBandwidthKbps
                    networkDetails = if (linkDown > 0) "${linkDown / 1000} Mbps" else "Connected"
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    networkType = "Cellular"
                    networkDetails = "Mobile Data Active"
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    networkType = "Ethernet"
                    networkDetails = "Wired Link"
                }
                else -> {
                    networkType = "Online"
                    networkDetails = "Connected"
                }
            }
        }

        // CPU
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuStatus = "$cpuCores Cores Active"

        return DeviceStatus(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            batteryTemperature = batteryTemp,
            usedRamGb = "%.1f".format(usedRamGb).toFloatOrNull() ?: usedRamGb,
            totalRamGb = "%.1f".format(totalRamGb).toFloatOrNull() ?: totalRamGb,
            freeRamGb = "%.1f".format(freeRamGb).toFloatOrNull() ?: freeRamGb,
            ramUsagePercent = ramUsagePct,
            usedStorageGb = "%.1f".format(usedStorageGb).toFloatOrNull() ?: usedStorageGb,
            totalStorageGb = "%.1f".format(totalStorageGb).toFloatOrNull() ?: totalStorageGb,
            storageUsagePercent = storageUsagePct,
            networkType = networkType,
            isNetworkConnected = isConnected,
            networkDetails = networkDetails,
            cpuStatus = cpuStatus
        )
    }
}
