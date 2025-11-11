package com.xr.common.middleware.base

import android.util.Log
import com.metabounds.libglass.v2.mbEnum.MBConnectivity

enum class DeviceType(val type: Short) {
    UNCONNECTED(-1), MOZART(0x0003), G12X1(0x0005), G07S5(0x0007), G12S0(0x0008), G07S3(0x0009);

    companion object {
        // 根据type获取对应的枚举名称
        fun fromType(type: Short): DeviceType? = values().find { it.type == type }
    }
}

fun String.getDeviceType(): DeviceType {
    if (this.length >= 8) {
        val strType = this.substring(4, 8)
        val type = strType.toShortOrNull(16) // 安全转换
        if (type != null) {
            val ret = DeviceType.fromType(type) ?: DeviceType.MOZART
            Log.d("getDeviceType", "type:$type,ret:$ret")
            return ret
        }
        Log.e("getDeviceType", "Invalid byte format in substring: $type")
    }
    return DeviceType.MOZART
}

fun String.getConnectType(): MBConnectivity {
    if (this.length >= 10) {
        val type = this.substring(8, 10)
        val typeByte = type.toByteOrNull(16) // 安全转换
        if (typeByte != null) {
            val ret = MBConnectivity.get(typeByte)
            Log.d("getConnectType", "type:$type,ret:$ret")
            return ret
        }
        Log.e("getConnectType", "Invalid byte format in substring: $type")
    }
    return MBConnectivity.BLE
}