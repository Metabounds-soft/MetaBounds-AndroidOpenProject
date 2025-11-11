package com.xr.common.middleware.model.bean

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index

@Entity(
    tableName = "device_info",
    primaryKeys = ["id"],
    indices = [Index(value = ["id"], unique = true)]
)
class NewDevice {
    @NonNull
    @ColumnInfo(name = "id")
    var id: Long = 0
    var uid: Long = 0
    var name: String? = null
    var model: String? = null
    var mac_bt: String? = null
    var mac_ble: String? = null
    var serial: String? = null
    var version: String? = null
    var token: String? = null
    var status: Int = 0
    var sync: Int = 0
    var enable_sms = false
    var enable_call_notif = false
    var master_app_ver: Short = 0
    var master_res_ver: Short = 0
    var slave_app_ver: Short = 0
    var sequence: Int = 0
    var time: Long = 0

    @Ignore
    var connect = 0

    @Ignore
    var batteryValue = 0

    @Ignore
    var batteryType = 0

    @Ignore
    var ringState = 0

}
/**
 *
 * uid 用户id
 *status = 0 // 未激活
 * = 1 // 已绑定
 * = 2 // 未绑定
 * = 3 // 挂失
 *
 * sync: 0表示不需要同步  1表示需要同步  2表示需要上传数据
 * enable_sms:是否开启消息通知
 * enable_call_notif:是否开启通知栏
 * master_app_ver:masterApp版本信息
 * master_res_ver:master资源版本号
 * slave_app_ver:slaveApp版本信息
 */