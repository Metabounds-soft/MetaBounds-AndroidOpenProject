package com.xr.common.middleware.network

import com.xr.common.middleware.base.Constants
import com.xr.common.middleware.network.interceptor.HeaderInterceptor
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Description:
 * CreateDate:     2023/5/17 11:46
 * Author:         agg
 */
object InterceptorManager {

    /**
     * 返回头拦截器
     * @return
     */
    fun headerInterceptor(): Interceptor = HeaderInterceptor()

    /**
     * 返回http拦截器
     */
    fun httpLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
        this.level =
            if (Constants.RELEASE) HttpLoggingInterceptor.Level.NONE else HttpLoggingInterceptor.Level.BODY
    }

    /**
     * 自定义http拦截器
     */
    var otherInterceptors: MutableList<Interceptor> = mutableListOf()

}