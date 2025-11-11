package com.xr.common.portal.feature.translate.model

import com.xr.common.middleware.base.Constants
import com.xr.common.middleware.model.data.UserV2DBManager
import com.xr.common.middleware.network.ApiFactory
import com.xr.common.middleware.network.KTBaseResponse
import com.xr.common.middleware.network.interceptor.HeaderInterceptor

/**
 * Description:
 * CreateDate:     2025/10/17 11:46
 * Author:         agg
 */
class HttpTranslateManager {
    private val localApi by lazy {
        ApiFactory().create(
            arrayOf(HeaderInterceptor()),
            Constants.THIRD_PARTY_HOST_TEST,
            HttpGoogleTranslateApi::class.java,
            Constants.NET_TIMEOUT,
            Constants.NET_TIMEOUT,
            Constants.NET_TIMEOUT
        )
    }

    suspend fun googleTranslate(
        sourceLang: String, targetLang: String, text: String
    ): KTBaseResponse<String?> {
        val hashMap = HashMap<String, String>()
        hashMap["sourceLang"] = sourceLang
        hashMap["targetLang"] = targetLang
        hashMap["text"] = text
        hashMap["username"] = (UserV2DBManager.getInstance().getUserInfo()?.username ?: "")
        return localApi.googleTranslate(hashMap)
    }
}