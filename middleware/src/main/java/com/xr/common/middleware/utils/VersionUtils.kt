package com.xr.common.middleware.utils

/**
 * Description:
 * CreateDate:     2023/5/17 15:26
 * Author:         agg
 */
object VersionUtils {
    //是否是debug模式
    @JvmStatic
    fun isDebug(): Boolean = "debug" == com.xr.common.middleware.BuildConfig.BUILD_TYPE

    //是否是发布模式
    @JvmStatic
    fun isRelease(): Boolean = "release" == com.xr.common.middleware.BuildConfig.BUILD_TYPE
}