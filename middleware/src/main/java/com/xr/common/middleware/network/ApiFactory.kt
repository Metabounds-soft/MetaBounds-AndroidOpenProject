package com.xr.common.middleware.network

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.xr.common.middleware.network.interceptor.MyLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Description:
 * CreateDate:     2023/5/17 11:06
 * Author:         agg
 */
class ApiFactory {
    companion object {
        /**
         * 连接超时时间
         */
        const val CONNECTTIMEOUT: Long = 15

        /**
         * 读取超时时间
         */
        const val READTIMEOUT: Long = 10

        /**
         * 写入超时时间
         */
        const val WRITETIMEOUT: Long = 10
    }

    //创建retrofit
    fun <T> create(
        interceptors: Array<Interceptor>? = null,
        baseUrl: String,
        clasz: Class<T>,
        connectionTime: Long = CONNECTTIMEOUT,
        readTimeOut: Long = READTIMEOUT,
        writetimeout: Long = WRITETIMEOUT
    ): T = Retrofit.Builder().baseUrl(baseUrl)
        .client(newClient(interceptors, connectionTime, readTimeOut, writetimeout))
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .addCallAdapterFactory(CoroutineCallAdapterFactory()).build().create(clasz)

    //配置OKHttp
    private fun newClient(
        interceptors: Array<Interceptor>?,
        connectionTime: Long = CONNECTTIMEOUT,
        readTimeOut: Long = READTIMEOUT,
        writetimeout: Long = WRITETIMEOUT
    ): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(connectionTime, TimeUnit.SECONDS)
        readTimeout(readTimeOut, TimeUnit.SECONDS)
        writeTimeout(writetimeout, TimeUnit.SECONDS).apply {
            interceptors?.forEach {
                this.addInterceptor(it)
            }
            addInterceptor(MyLoggingInterceptor())
        }
    }.build()

}