package com.xr.common.middleware.model.bean

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class GalleryMediaData(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    @NonNull @ColumnInfo(name = "mediaType") var mediaType: Int = -1, //数据类型，1：图片，2：视频
    @NonNull @ColumnInfo(name = "appSavePath") var appSavePath: String = "", //数据在app内部的保存路径
    @NonNull @ColumnInfo(name = "systemSavePath") var systemSavePath: String = "",//数据在系统中的保存路径
    @NonNull @ColumnInfo(name = "userId") var userId: Long = -1L, //用户ID
    @NonNull @ColumnInfo(name = "timestamp") var timestamp: Long = -1L, //时间戳
    @NonNull @ColumnInfo(name = "isStabilization") var isStabilization: Boolean = false, //是否进行了防抖
    @NonNull @ColumnInfo(name = "stabilizationDir") var stabilizationDir: String = "" //防抖数据文件路径
) {
    companion object {
        const val TYPE_PHOTO = 1
        const val TYPE_VIDEO = 2
    }
}