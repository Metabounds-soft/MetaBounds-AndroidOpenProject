package com.xr.common.middleware.utils

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.provider.Settings


fun Context.checkOpenBluetooth(): Boolean {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        return false
    }
    return true
}


fun Context.requestOpenBluetooth() {
    try {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivity(enableIntent)
    } catch (e: Exception) {

    }

}

fun Context.startSettingOpenBluetooth() {
    val enableIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    startActivity(enableIntent)
}