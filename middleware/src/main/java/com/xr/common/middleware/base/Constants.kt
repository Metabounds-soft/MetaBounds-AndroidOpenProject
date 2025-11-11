package com.xr.common.middleware.base

import com.xr.common.middleware.utils.VersionUtils

/**
 * Description:
 * CreateDate:     2023/5/17 11:13
 * Author:         agg
 */
object Constants {

    val RELEASE = VersionUtils.isRelease()

    const val CUSTOMER_TAG = "MOJIE"
    const val THIRD_PARTY_HOST_TEST = "http://8.219.215.141:8091"
    const val NET_TIMEOUT: Long = 60
    const val LOW_BATTERY: Long = 20

    val rawPath = BaseLibData.application.getExternalFilesDir(null)?.absolutePath + "/vel/raw"
    var systemPicturePath = "/storage/emulated/0/Pictures/vel/picture" // 图片在系统中的存储路径
    var velPicturePath =
        BaseLibData.application.getExternalFilesDir(null)?.absolutePath + "/vel/picture" // 图片app存储路径
    var velPath = BaseLibData.application.getExternalFilesDir(null)?.absolutePath + "/vel"
    var tempPath = BaseLibData.application.getExternalFilesDir(null)?.absolutePath + "/vel/temp"
    var downloadDir: String = "/storage/emulated/0/Download/"
    var binPath = BaseLibData.application.getExternalFilesDir(null)?.absolutePath + "/vel/bin"

}