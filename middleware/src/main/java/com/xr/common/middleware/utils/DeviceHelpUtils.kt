package com.xr.common.middleware.utils

import android.content.Context
import com.blankj.utilcode.util.ToastUtils
import com.xr.common.middleware.R
import com.xr.common.middleware.manager.MBDeviceManager

/**
 * Description:
 * CreateDate:     2025/10/28 11:03
 * Author:         agg
 */

fun Context.checkDeviceConnect(invoke: () -> Unit): Boolean {
    if (!checkOpenBluetooth()) {
        ToastUtils.showLong(getString(R.string.bluetooth_no_open_tip))
        return false
    }
    if (!MBDeviceManager.getInstance().isConnected()) {
        ToastUtils.showLong("${getString(R.string.device_no_connect_tip)}.")
        return false
    }
    invoke.invoke()
    return true
}