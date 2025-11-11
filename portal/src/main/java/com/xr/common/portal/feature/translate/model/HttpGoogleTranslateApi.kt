package com.xr.common.portal.feature.translate.model

import com.xr.common.middleware.network.KTBaseResponse
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Description:
 * CreateDate:     2025/10/17 13:45
 * Author:         agg
 */
interface HttpGoogleTranslateApi {

    @FormUrlEncoded
    @POST("/api/v1/google/translate")//获取设备token
    suspend fun googleTranslate(@FieldMap hashMap: HashMap<String, String>): KTBaseResponse<String?>

}